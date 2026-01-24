#ifndef SHA1_CUDA_CUH
#define SHA1_CUDA_CUH
// a single message per thread (DATA + NONCE)
#include <cuda_runtime.h> //CUDA library
#include <stdint.h> //for standard integer types

#define SHA1_DIGEST_SIZE 20
#define SHA1_BLOCK_SIZE 64
//the __device__ functions will run on the GPU, not CPU
//__forceinline__ makes the compiler to insert the actual code instead of calling the function => faster
__device__ __forceinline__ uint32_t rotl32(uint32_t x, int n) {
    return (x << n) | (x >> (32 - n));
}

__device__ __forceinline__ uint32_t f0(uint32_t b, uint32_t c, uint32_t d) {
    return (b & c) | ((~b) & d);
}

__device__ __forceinline__ uint32_t f1(uint32_t b, uint32_t c, uint32_t d) {
    return b ^ c ^ d;
}

__device__ __forceinline__ uint32_t f2(uint32_t b, uint32_t c, uint32_t d) {
    return (b & c) | (b & d) | (c & d);
}

__device__ __forceinline__ uint32_t f3(uint32_t b, uint32_t c, uint32_t d) {
    return b ^ c ^ d;
}

//__constant__ stores this in constant memory which is fast;it's cached and broadcast to all threads at once, value never changes
__device__ __constant__ uint32_t K[4] = {
    0x5A827999,
    0x6ED9EBA1,
    0x8F1BBCDC,
    0xCA62C1D6
};

__device__ __constant__ uint32_t H0[5] = {
    0x67452301,
    0xEFCDAB89,
    0x98BADCFE,
    0x10325476,
    0xC3D2E1F0
};

__device__ void sha1_short(const uint8_t* msg, uint32_t msg_len, uint8_t* hash) {
    uint32_t w[80];
    uint32_t a, b, c, d, e;

    #pragma unroll //doesn't loop, writes out all iterations as separate lines => runs faster
    for (int i = 0; i < 16; i++) {
        w[i] = 0;
    }

    for (uint32_t i = 0; i < msg_len; i++) {
        w[i >> 2] |= ((uint32_t)msg[i]) << (24 - (i & 3) * 8);
    }

    w[msg_len >> 2] |= 0x80 << (24 - (msg_len & 3) * 8);

    uint64_t bit_len = (uint64_t)msg_len * 8;
    w[14] = (uint32_t)(bit_len >> 32);
    w[15] = (uint32_t)bit_len;

    #pragma unroll
    for (int i = 16; i < 80; i++) {
        w[i] = rotl32(w[i-3] ^ w[i-8] ^ w[i-14] ^ w[i-16], 1);
    }

    a = H0[0];
    b = H0[1];
    c = H0[2];
    d = H0[3];
    e = H0[4];

    uint32_t temp;

    #pragma unroll
    for (int i = 0; i < 20; i++) {
        temp = rotl32(a, 5) + f0(b, c, d) + e + K[0] + w[i];
        e = d;
        d = c;
        c = rotl32(b, 30);
        b = a;
        a = temp;
    }

    #pragma unroll
    for (int i = 20; i < 40; i++) {
        temp = rotl32(a, 5) + f1(b, c, d) + e + K[1] + w[i];
        e = d;
        d = c;
        c = rotl32(b, 30);
        b = a;
        a = temp;
    }

    #pragma unroll
    for (int i = 40; i < 60; i++) {
        temp = rotl32(a, 5) + f2(b, c, d) + e + K[2] + w[i];
        e = d;
        d = c;
        c = rotl32(b, 30);
        b = a;
        a = temp;
    }

    #pragma unroll
    for (int i = 60; i < 80; i++) {
        temp = rotl32(a, 5) + f3(b, c, d) + e + K[3] + w[i];
        e = d;
        d = c;
        c = rotl32(b, 30);
        b = a;
        a = temp;
    }

    a += H0[0];
    b += H0[1];
    c += H0[2];
    d += H0[3];
    e += H0[4];

    hash[0]  = (a >> 24) & 0xFF;
    hash[1]  = (a >> 16) & 0xFF;
    hash[2]  = (a >> 8) & 0xFF;
    hash[3]  = a & 0xFF;
    hash[4]  = (b >> 24) & 0xFF;
    hash[5]  = (b >> 16) & 0xFF;
    hash[6]  = (b >> 8) & 0xFF;
    hash[7]  = b & 0xFF;
    hash[8]  = (c >> 24) & 0xFF;
    hash[9]  = (c >> 16) & 0xFF;
    hash[10] = (c >> 8) & 0xFF;
    hash[11] = c & 0xFF;
    hash[12] = (d >> 24) & 0xFF;
    hash[13] = (d >> 16) & 0xFF;
    hash[14] = (d >> 8) & 0xFF;
    hash[15] = d & 0xFF;
    hash[16] = (e >> 24) & 0xFF;
    hash[17] = (e >> 16) & 0xFF;
    hash[18] = (e >> 8) & 0xFF;
    hash[19] = e & 0xFF;
}

__device__ __forceinline__ bool check_suffix(const uint8_t* hash, const uint8_t* suffix, int suffix_len) {
    for (int i = 0; i < suffix_len; i++) {
        if (hash[SHA1_DIGEST_SIZE - suffix_len + i] != suffix[i]) {
            return false;
        }
    }
    return true;
}

#endif
