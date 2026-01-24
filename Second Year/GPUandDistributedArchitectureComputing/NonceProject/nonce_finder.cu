#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <cuda_runtime.h>

#include "sha1_cuda.cuh"

#define MAX_DATA_SIZE 48 //max input data size
#define MAX_NONCE_SIZE 8 //8 bytes maximum
#define MAX_SUFFIX_SIZE 2 //1 or 2 byte suffixes


//constant: read-only from device code, cached and optimized for broadcast access
__constant__ uint8_t d_data[MAX_DATA_SIZE]; //the data to hash
__constant__ uint8_t d_suffix[MAX_SUFFIX_SIZE]; // the suffix
__constant__ int d_data_len; //actual length of data
__constant__ int d_suffix_len; //actual length of suffix


/*
 * Global device variables for inter-thread and host-device communication
 * These are in global memory, accessible by all threads and the host.
 * We use atomic operations on d_found to safely coordinate between threads.
 */
__device__ int d_found; //0-not found, 1-found
__device__ uint64_t d_result_nonce; //the winning nonce

//converts nonce to bytes (nonce=256    -> 2 bytes: [0x00, 0x01])
__device__ int nonce_to_bytes(uint64_t nonce, uint8_t* bytes) {
    int len = 0;
    uint64_t temp = nonce;

    //count how many bytes we need
    do {
        len++;
        temp >>= 8; //equivalent with dividing to 256
    } while (temp > 0);

    //write in little endian order
    for (int i = 0; i < len; i++) {
        bytes[i] = (nonce >> (i * 8)) & 0xFF;
    }

    return len;
}

__global__ void find_nonce_kernel(uint64_t start_nonce, int nonces_per_thread) {
    if (d_found) return; // if another thread found the answer there is no point in searching

    uint64_t tid = blockIdx.x * blockDim.x + threadIdx.x; //calculate the global thread id

    //calculate the start nonce for this thread
    // Thread 0 handles [start_nonce, start_nonce + nonces_per_thread)
    // Thread 1 handles [start_nonce + nonces_per_thread, start_nonce + 2*nonces_per_thread)
    uint64_t my_start = start_nonce + tid * nonces_per_thread;

    uint8_t msg[MAX_DATA_SIZE + MAX_NONCE_SIZE]; //message buffer (it will hold data+nonce)
    uint8_t hash[SHA1_DIGEST_SIZE]; //sha1 output buffer
    uint8_t nonce_bytes[MAX_NONCE_SIZE]; //nonce converted to byte

    // copy DATA from constant memory to local buffer
    // this read is cached and broadcast-efficient since all threads read the same data
    // we only need to copy once, then append different nonces
    for (int i = 0; i < d_data_len; i++) {
        msg[i] = d_data[i];
    }

    for (int i = 0; i < nonces_per_thread; i++) { //we take the nonces to test
    	// periodically check if another thread found the answer (every 64 iterations)
    	// why not check every iteration? Reading d_found is a global memory access,
    	// which is slow. Checking every 64 iterations balances responsiveness vs overhead
        if (i % 64 == 0 && d_found) return;

        uint64_t nonce = my_start + i; //calculate the nonce i want to test

        //WE CONVERT TO BYTES BECAUSE SHA1 OPERATES ON BYTE SEQUENCES, NOT INTEGERS
        int nonce_len = nonce_to_bytes(nonce, nonce_bytes); //convert nonce to byte

        for (int j = 0; j < nonce_len; j++) { //append the nonce bytes after data in msg buffer: [DATA bytes][NONCE bytes]
            msg[d_data_len + j] = nonce_bytes[j];
        }

        int msg_len = d_data_len + nonce_len; //total msg length: data + nonce len

        // compute SHA1 hash of the complete message (DATA + NONCE)
        // sha1_short is optimized for messages that fit in a single SHA1 block (<=55 bytes)
        sha1_short(msg, msg_len, hash);

        if (check_suffix(hash, d_suffix, d_suffix_len)) {//check if hash ends with suffix
        	// atomicCAS(&d_found, 0, 1) does: if d_found == 0, set it to 1
        	// It returns the OLD value of d_found
        	// If old == 0, we were the first thread to find it (we won the race)
        	// If old == 1, another thread already found it (we lost the race)
            int old = atomicCAS(&d_found, 0, 1);
            if (old == 0) {
                d_result_nonce = nonce;
            }
            return;
        }
    }
    // if we get here, none of our nonces produced the target suffix
    // the host will launch another kernel with a new range of nonces
}


// Shared memory is useful when:
//- Multiple threads in a block need to share data
//- Data is reused many times within a block
//- You want to avoid repeated global/constant memory accesses
// In this kernel, we load the suffix into shared memory once per block,
// then all threads in that block read from fast shared memory.
__global__ void find_nonce_kernel_shared(uint64_t start_nonce, int nonces_per_thread) {
	// declare shared memory - allocated per-block, shared among all threads in the block
	// __shared__ variables persist for the lifetime of the block
    __shared__ uint8_t s_suffix[MAX_SUFFIX_SIZE];
    __shared__ int s_suffix_len;

    if (threadIdx.x == 0) { //only thread 0 loads the suffix into shared memory
        s_suffix_len = d_suffix_len; //copy from const memory
        for (int i = 0; i < d_suffix_len; i++) {
            s_suffix[i] = d_suffix[i];
        }
    }

    // SYNCHRONIZATION BARRIER: all threads in the block wait here
    // this ensures thread 0 has finished loading before others read shared memory
    // without this, other threads might read uninitialized shared memory!
    __syncthreads();

    if (d_found) return; //exit if already found

    uint64_t tid = blockIdx.x * blockDim.x + threadIdx.x;
    uint64_t my_start = start_nonce + tid * nonces_per_thread;

    uint8_t msg[MAX_DATA_SIZE + MAX_NONCE_SIZE];
    uint8_t hash[SHA1_DIGEST_SIZE];
    uint8_t nonce_bytes[MAX_NONCE_SIZE];

    for (int i = 0; i < d_data_len; i++) {
        msg[i] = d_data[i];
    }

    for (int i = 0; i < nonces_per_thread; i++) { //check each nonce
        if (i % 64 == 0 && d_found) return;

        uint64_t nonce = my_start + i;
        int nonce_len = nonce_to_bytes(nonce, nonce_bytes);

        for (int j = 0; j < nonce_len; j++) {
            msg[d_data_len + j] = nonce_bytes[j];
        }

        sha1_short(msg, d_data_len + nonce_len, hash); //compute sha1

        //check suffix using shared memory instead const memory
        bool match = true;
        for (int k = 0; k < s_suffix_len; k++) {
        	// hash[20 - suffix_len + k] should equal s_suffix[k]
        	// for suffix_len=1: compare hash[19] with suffix[0]
        	// for suffix_len=2: compare hash[18],hash[19] with suffix[0],suffix[1]
            if (hash[SHA1_DIGEST_SIZE - s_suffix_len + k] != s_suffix[k]) {
                match = false;
                break;
            }
        }

        if (match) {
            int old = atomicCAS(&d_found, 0, 1);
            if (old == 0) {
                d_result_nonce = nonce;
            }
            return;
        }
    }
}

//HANDLE_ERROR from book (check CUDA API status code)
#define CUDA_CHECK(call) \
    do { \
        cudaError_t err = call; \
        if (err != cudaSuccess) { \
            fprintf(stderr, "CUDA error at %s:%d: %s\n", __FILE__, __LINE__, \
                    cudaGetErrorString(err)); \
            exit(EXIT_FAILURE); \
        } \
    } while(0)

// helper function to print a byte array as a hexadecimal string
// print_hex("Hash", {0xab, 0xcd, 0xef}, 3) prints "Hash: abcdef"
void print_hex(const char* label, const uint8_t* data, int len) {
    printf("%s: ", label);
    for (int i = 0; i < len; i++) {
        printf("%02x", data[i]);
    }
    printf("\n");
}

// CPU implementation of SHA1 hash algorithm (for verification)
void sha1_cpu(const uint8_t* msg, int msg_len, uint8_t* hash) {
    uint32_t w[80] = {0};
    uint32_t h[5] = {0x67452301, 0xEFCDAB89, 0x98BADCFE, 0x10325476, 0xC3D2E1F0};

    for (int i = 0; i < msg_len; i++) {
        w[i >> 2] |= ((uint32_t)msg[i]) << (24 - (i & 3) * 8);
    }

    w[msg_len >> 2] |= 0x80 << (24 - (msg_len & 3) * 8);
    w[15] = msg_len * 8;

    for (int i = 16; i < 80; i++) {
        uint32_t temp = w[i-3] ^ w[i-8] ^ w[i-14] ^ w[i-16];
        w[i] = (temp << 1) | (temp >> 31);
    }

    uint32_t a = h[0], b = h[1], c = h[2], d = h[3], e = h[4];
    uint32_t k[4] = {0x5A827999, 0x6ED9EBA1, 0x8F1BBCDC, 0xCA62C1D6};

    for (int i = 0; i < 80; i++) {
        uint32_t f, ki;
        if (i < 20) { f = (b & c) | ((~b) & d); ki = k[0]; }
        else if (i < 40) { f = b ^ c ^ d; ki = k[1]; }
        else if (i < 60) { f = (b & c) | (b & d) | (c & d); ki = k[2]; }
        else { f = b ^ c ^ d; ki = k[3]; }

        uint32_t temp = ((a << 5) | (a >> 27)) + f + e + ki + w[i];
        e = d; d = c; c = (b << 30) | (b >> 2); b = a; a = temp;
    }

    h[0] += a; h[1] += b; h[2] += c; h[3] += d; h[4] += e;

    for (int i = 0; i < 5; i++) {
        hash[i*4] = (h[i] >> 24) & 0xFF;
        hash[i*4+1] = (h[i] >> 16) & 0xFF;
        hash[i*4+2] = (h[i] >> 8) & 0xFF;
        hash[i*4+3] = h[i] & 0xFF;
    }
}

// configuration structure for kernel launch parameters
typedef struct {
    int block_size;
    int nonces_per_thread;
    int num_blocks;
    bool use_shared_memory;
} Config;

//returns true if nonce found, false if iteration limit reached
bool find_nonce(const uint8_t* data, int data_len,
                const uint8_t* suffix, int suffix_len,
                Config config, uint64_t* found_nonce,
                double* elapsed_ms, uint64_t* total_hashes) {

	//copy input data from host to GPU constant memory
    CUDA_CHECK(cudaMemcpyToSymbol(d_data, data, data_len));
    CUDA_CHECK(cudaMemcpyToSymbol(d_suffix, suffix, suffix_len));
    CUDA_CHECK(cudaMemcpyToSymbol(d_data_len, &data_len, sizeof(int)));
    CUDA_CHECK(cudaMemcpyToSymbol(d_suffix_len, &suffix_len, sizeof(int)));

    //reset the d_found flag to 0 (nonce was not found yet)
    int zero = 0;
    CUDA_CHECK(cudaMemcpyToSymbol(d_found, &zero, sizeof(int)));

    //calculate how many nonces are checked in one kernel launch
    uint64_t nonces_per_batch = (uint64_t)config.num_blocks *
                                (uint64_t)config.block_size *
                                (uint64_t)config.nonces_per_thread;

    printf("Configuration: blocks=%d, threads/block=%d, nonces/thread=%d\n",
           config.num_blocks, config.block_size, config.nonces_per_thread);
    printf("Nonces per batch: %lu\n", (unsigned long)nonces_per_batch);
    printf("Using %s kernel\n", config.use_shared_memory ? "shared memory" : "standard");

    // set up CUDA events for GPU timing
    // CUDA events measure time on the GPU, more accurate than CPU timers for GPU work
    cudaEvent_t start, stop;
    CUDA_CHECK(cudaEventCreate(&start));
    CUDA_CHECK(cudaEventCreate(&stop));
    CUDA_CHECK(cudaEventRecord(start));

    uint64_t start_nonce = 0; //first nonce to try
    int found = 0; //if found or not
    uint64_t max_iterations = 1000000;
    uint64_t iteration = 0;

    while (!found && iteration < max_iterations) {
        if (config.use_shared_memory) {
            find_nonce_kernel_shared<<<config.num_blocks, config.block_size>>>(
                start_nonce, config.nonces_per_thread);
        } else {
            find_nonce_kernel<<<config.num_blocks, config.block_size>>>(
                start_nonce, config.nonces_per_thread);
        }

        //check for kernel errors
        CUDA_CHECK(cudaGetLastError());
        CUDA_CHECK(cudaDeviceSynchronize());

        //copy flag from GPU to host to check if we re done
        CUDA_CHECK(cudaMemcpyFromSymbol(&found, d_found, sizeof(int)));

        start_nonce += nonces_per_batch;
        iteration++;

        if (iteration % 100 == 0) {
            printf("Progress: %lu hashes checked...\n",
                   (unsigned long)(iteration * nonces_per_batch));
        }
    } //end while

    CUDA_CHECK(cudaEventRecord(stop));
    CUDA_CHECK(cudaEventSynchronize(stop)); //wait for stop event to complete
    float elapsed_float;
    CUDA_CHECK(cudaEventElapsedTime(&elapsed_float, start, stop));
    *elapsed_ms = (double)elapsed_float;

    //clean up cuda events
    CUDA_CHECK(cudaEventDestroy(start));
    CUDA_CHECK(cudaEventDestroy(stop));

    // calculate approximate total hashes (slight overestimate since we don't know
    // exactly which nonce in the batch was the winner)
    *total_hashes = iteration * nonces_per_batch;

    if (found) {
        uint64_t result;
        //copy winning nonce from GPU to host
        CUDA_CHECK(cudaMemcpyFromSymbol(&result, d_result_nonce, sizeof(uint64_t)));
        *found_nonce = result;
        return true;
    }

    return false;
}

// converts ASCII hex representation to actual byte values.
// xxample: "48656c6c6f" -> {0x48, 0x65, 0x6c, 0x6c, 0x6f} = "Hello"
int hex_to_bytes(const char* hex, uint8_t* bytes, int max_len) {
    int len = strlen(hex);
    if (len % 2 != 0) return -1;

    int byte_len = len / 2;
    if (byte_len > max_len) return -1;

    for (int i = 0; i < byte_len; i++) {
        char byte_str[3] = {hex[i*2], hex[i*2+1], 0};
        bytes[i] = (uint8_t)strtol(byte_str, NULL, 16);
    }

    return byte_len;
}

void print_usage(const char* prog) {
    printf("Usage: %s [options]\n\n", prog);
    printf("Options:\n");
    printf("  -d <hex>      DATA as hex string (default: test data)\n");
    printf("  -s <hex>      SUFFIX as hex string, 1-2 bytes (default: ff)\n");
    printf("  -b <int>      Block size (threads per block, default: 256)\n");
    printf("  -n <int>      Nonces per thread (default: 100)\n");
    printf("  -g <int>      Number of blocks (grid size, default: 256)\n");
    printf("  -m            Use shared memory kernel variant\n");
    printf("  -a            Run all configurations (benchmark mode)\n");
    printf("  -h            Show this help\n");
    printf("\nExample:\n");
    printf("  %s -d 48656c6c6f -s ff\n", prog);
    printf("  Finds nonce where SHA1(\"Hello\" + nonce) ends with 0xff\n");
}

int main(int argc, char** argv) {
    uint8_t data[MAX_DATA_SIZE] = "TestData";
    int data_len = 8;
    uint8_t suffix[MAX_SUFFIX_SIZE] = {0xff};
    int suffix_len = 1;

    Config config = {
        .block_size = 256,
        .nonces_per_thread = 100,
        .num_blocks = 256,
        .use_shared_memory = false
    };

    bool benchmark_mode = false;

    for (int i = 1; i < argc; i++) {
        if (strcmp(argv[i], "-d") == 0 && i + 1 < argc) {
            data_len = hex_to_bytes(argv[++i], data, MAX_DATA_SIZE);
            if (data_len < 0) {
                fprintf(stderr, "Invalid DATA hex string\n");
                return 1;
            }
        } else if (strcmp(argv[i], "-s") == 0 && i + 1 < argc) {
            suffix_len = hex_to_bytes(argv[++i], suffix, MAX_SUFFIX_SIZE);
            if (suffix_len < 0 || suffix_len > 2) {
                fprintf(stderr, "SUFFIX must be 1-2 bytes\n");
                return 1;
            }
        } else if (strcmp(argv[i], "-b") == 0 && i + 1 < argc) {
            config.block_size = atoi(argv[++i]);
        } else if (strcmp(argv[i], "-n") == 0 && i + 1 < argc) {
            config.nonces_per_thread = atoi(argv[++i]);
        } else if (strcmp(argv[i], "-g") == 0 && i + 1 < argc) {
            config.num_blocks = atoi(argv[++i]);
        } else if (strcmp(argv[i], "-m") == 0) {
            config.use_shared_memory = true;
        } else if (strcmp(argv[i], "-a") == 0) {
            benchmark_mode = true;
        } else if (strcmp(argv[i], "-h") == 0) {
            print_usage(argv[0]);
            return 0;
        }
    }

    int device;
    cudaDeviceProp prop;
    CUDA_CHECK(cudaGetDevice(&device));
    CUDA_CHECK(cudaGetDeviceProperties(&prop, device));

    printf("=== CUDA Nonce Finder ===\n");
    printf("GPU: %s\n", prop.name);
    printf("Compute capability: %d.%d\n", prop.major, prop.minor);
    printf("Max threads per block: %d\n", prop.maxThreadsPerBlock);
    printf("Multiprocessors: %d\n", prop.multiProcessorCount);
    printf("Total global memory: %.2f GB\n\n", prop.totalGlobalMem / 1e9);

    print_hex("DATA", data, data_len);
    print_hex("SUFFIX", suffix, suffix_len);
    printf("\n");

    if (benchmark_mode) {
        Config configs[] = {
            {128, 50, 256, false},
            {128, 100, 256, false},
            {256, 50, 256, false},
            {256, 100, 256, false},
            {256, 200, 256, false},
            {512, 50, 256, false},
            {512, 100, 256, false},
            {256, 100, 512, false},
            {256, 100, 1024, false},
            {256, 100, 256, true},
            {512, 100, 256, true},
        };
        int num_configs = sizeof(configs) / sizeof(configs[0]);

        printf("=== Benchmark Mode ===\n\n");

        for (int i = 0; i < num_configs; i++) {
            printf("--- Configuration %d ---\n", i + 1);

            uint64_t found_nonce;
            double elapsed_ms;
            uint64_t total_hashes;

            bool success = find_nonce(data, data_len, suffix, suffix_len,
                                      configs[i], &found_nonce,
                                      &elapsed_ms, &total_hashes);

            if (success) {
                uint8_t verify_msg[MAX_DATA_SIZE + MAX_NONCE_SIZE];
                memcpy(verify_msg, data, data_len);

                uint64_t nonce = found_nonce;
                int nonce_len = 0;
                do {
                    verify_msg[data_len + nonce_len++] = nonce & 0xFF;
                    nonce >>= 8;
                } while (nonce > 0 || nonce_len == 0);

                uint8_t verify_hash[SHA1_DIGEST_SIZE];
                sha1_cpu(verify_msg, data_len + nonce_len, verify_hash);

                printf("Found nonce: %lu (0x%lx)\n",
                       (unsigned long)found_nonce, (unsigned long)found_nonce);
                print_hex("Hash", verify_hash, SHA1_DIGEST_SIZE);

                double hash_rate = total_hashes / (elapsed_ms / 1000.0);
                printf("Time: %.2f ms\n", elapsed_ms);
                printf("Hash rate: %.2f MH/s\n", hash_rate / 1e6);
            } else {
                printf("Nonce not found within limit\n");
            }
            printf("\n");
        }

    } else {
        uint64_t found_nonce;
        double elapsed_ms;
        uint64_t total_hashes;

        bool success = find_nonce(data, data_len, suffix, suffix_len,
                                  config, &found_nonce,
                                  &elapsed_ms, &total_hashes);

        if (success) {
            uint8_t nonce_bytes[MAX_NONCE_SIZE];
            uint64_t temp = found_nonce;
            int nonce_len = 0;
            do {
                nonce_bytes[nonce_len++] = temp & 0xFF;
                temp >>= 8;
            } while (temp > 0);

            printf("\n=== RESULT ===\n");
            printf("Found nonce: %lu (0x%lx)\n",
                   (unsigned long)found_nonce, (unsigned long)found_nonce);
            print_hex("NONCE bytes", nonce_bytes, nonce_len);

            uint8_t verify_msg[MAX_DATA_SIZE + MAX_NONCE_SIZE];
            memcpy(verify_msg, data, data_len);
            memcpy(verify_msg + data_len, nonce_bytes, nonce_len);

            uint8_t verify_hash[SHA1_DIGEST_SIZE];
            sha1_cpu(verify_msg, data_len + nonce_len, verify_hash);

            print_hex("SHA1(DATA+NONCE)", verify_hash, SHA1_DIGEST_SIZE);

            bool verified = true;
            for (int i = 0; i < suffix_len; i++) {
                if (verify_hash[SHA1_DIGEST_SIZE - suffix_len + i] != suffix[i]) {
                    verified = false;
                    break;
                }
            }

            printf("Verification: %s\n", verified ? "PASSED" : "FAILED");

            double hash_rate = total_hashes / (elapsed_ms / 1000.0);
            printf("\nPerformance:\n");
            printf("  Time: %.2f ms\n", elapsed_ms);
            printf("  Hashes checked: ~%lu\n", (unsigned long)total_hashes);
            printf("  Hash rate: %.2f MH/s\n", hash_rate / 1e6);

        } else {
            printf("Nonce not found within iteration limit.\n");
            printf("Try with a shorter suffix or increase max_iterations.\n");
        }
    }

    return 0;
}
