#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include "ult.h"

/* ============================================================
 * Test 1: Basic Thread Creation and Join
 * ============================================================ */

void *simple_thread(void *arg) {
    int id = *(int *)arg;
    printf("[Thread %d] Started (tid=%d)\n", id, ult_self());

    /* Do some work */
    for (int i = 0; i < 3; i++) {
        printf("[Thread %d] Working... iteration %d\n", id, i);
        ult_yield();
    }

    printf("[Thread %d] Exiting\n", id);
    return (void *)(long)(id * 10);
}

void test_basic_threads(void) {
    printf("\n========== TEST 1: Basic Thread Creation ==========\n");

    ult_t threads[3];
    int ids[3] = {1, 2, 3};

    /* Create threads */
    for (int i = 0; i < 3; i++) {
        if (ult_create(&threads[i], simple_thread, &ids[i]) != 0) {
            printf("Failed to create thread %d\n", i);
            return;
        }
        printf("[Main] Created thread %d with tid=%d\n", ids[i], threads[i]);
    }

    /* Join threads and get return values */
    for (int i = 0; i < 3; i++) {
        void *retval;
        ult_join(threads[i], &retval);
        printf("[Main] Thread %d returned: %ld\n", ids[i], (long)retval);
    }

    printf("[Main] Test 1 completed!\n");
}

/* ============================================================
 * Test 2: Preemptive Scheduling
 * ============================================================ */

volatile int preempt_counter[3] = {0, 0, 0};

void *preemptive_thread(void *arg) { // threads never call ult_yield() :)
    int id = *(int *)arg;
    printf("[Preempt Thread %d] Started\n", id);

    // busy loop - will be preempted
    for (int i = 0; i < 5000000; i++) {
        preempt_counter[id]++;
        if (preempt_counter[id] % 250000 == 0) {
            printf("[Preempt Thread %d] Counter: %d\n", id, preempt_counter[id]);
        }
    }

    printf("[Preempt Thread %d] Finished with counter=%d\n", id, preempt_counter[id]);
    return NULL;
}

void test_preemption(void) {
    printf("\n========== TEST 2: Preemptive Scheduling ==========\n");
    printf("(Threads should interleave due to preemption)\n");

    ult_t threads[3];
    int ids[3] = {0, 1, 2};

    for (int i = 0; i < 3; i++) {
        preempt_counter[i] = 0;
        ult_create(&threads[i], preemptive_thread, &ids[i]);
    }

    for (int i = 0; i < 3; i++) {
        ult_join(threads[i], NULL);
    }

    printf("[Main] All preemptive threads completed!\n");
}

/* ============================================================
 * Test 3: Mutex - Protected Counter
 * ============================================================ */

ult_mutex_t counter_mutex; //the lock
int shared_counter = 0; //what we're protecting
#define INCREMENTS_PER_THREAD 10000

void *mutex_thread(void *arg) {
    int id = *(int *)arg; //get thread id

    for (int i = 0; i < INCREMENTS_PER_THREAD; i++) {
        ult_mutex_lock(&counter_mutex); //lock
        shared_counter++;//increment
        ult_mutex_unlock(&counter_mutex);//decrement
    }

    printf("[Mutex Thread %d] Done incrementing\n", id);
    return NULL;
}

void test_mutex(void) {
    printf("\n========== TEST 3: Mutex Protected Counter ==========\n");

    ult_mutex_init(&counter_mutex); //initialize lock
    shared_counter = 0; //start counter ar 0

    ult_t threads[4];
    int ids[4] = {1, 2, 3, 4};

    for (int i = 0; i < 4; i++) {
        ult_create(&threads[i], mutex_thread, &ids[i]); //create 4 threads (the id will be the argument for mutex thread)
    }

    for (int i = 0; i < 4; i++) {
        ult_join(threads[i], NULL); //wait for all threads to finish
    }

    int expected = 4 * INCREMENTS_PER_THREAD;
    printf("[Main] Final counter: %d (expected: %d)\n", shared_counter, expected); //check if we obtained the expected

    if (shared_counter == expected) {
        printf("[Main] SUCCESS! Mutex correctly protected the counter.\n");
    } else {
        printf("[Main] FAILURE! Race condition detected.\n");
    }

    ult_mutex_destroy(&counter_mutex); //destroy the mutex
}

/* ============================================================
 * Test 4: Semaphore - Producer/Consumer
 * ============================================================ */

#define BUFFER_SIZE 5
#define NUM_ITEMS 10 //producer will make 10 items

ult_sem_t empty_slots; //counts empty spaces in buffer
ult_sem_t full_slots; //counts filled spaces in buffer
ult_mutex_t buffer_mutex; //protects buffer access
//( Mutex handles exclusive access to buffer. Without mutex, two consumers might read the same slot)
int buffer[BUFFER_SIZE]; //circular buffer
int buf_in = 0; //where to put the next item
int buf_out = 0; //where to take the next item

void *producer(void *arg) {
    (void)arg;

    for (int i = 1; i <= NUM_ITEMS; i++) { // make 10 items
        ult_sem_wait(&empty_slots); //wait for empty slot
        ult_mutex_lock(&buffer_mutex); //lock buffer access

        buffer[buf_in] = i; //puts item in buffer
        printf("[Producer] Produced item %d at position %d\n", i, buf_in);
        buf_in = (buf_in + 1) % BUFFER_SIZE; //moves to the next slot

        ult_mutex_unlock(&buffer_mutex); //unlocks the buffer
        ult_sem_post(&full_slots); // tells a consumer that a new item is available
    }

    printf("[Producer] Done producing\n");
    return NULL;
}

void *consumer(void *arg) {
    int id = *(int *)arg; //id of the consumer

    for (int i = 0; i < NUM_ITEMS / 2; i++) { //each consumer takes 5 items
        ult_sem_wait(&full_slots); //wait for item to exist
        ult_mutex_lock(&buffer_mutex); //lock buffer access

        int item = buffer[buf_out]; //take item from buffer
        printf("[Consumer %d] Consumed item %d from position %d\n", id, item, buf_out);
        buf_out = (buf_out + 1) % BUFFER_SIZE; //move to the next slot

        ult_mutex_unlock(&buffer_mutex); //unlock the mutex
        ult_sem_post(&empty_slots); //signal that there is one more empty slot
    }

    printf("[Consumer %d] Done consuming\n", id);
    return NULL;
}

void test_semaphore(void) {
    printf("\n========== TEST 4: Semaphore Producer/Consumer ==========\n");

    ult_sem_init(&empty_slots, BUFFER_SIZE); //5 slots initially
    ult_sem_init(&full_slots, 0); //o slots initially
    ult_mutex_init(&buffer_mutex); //initialize the buffer lock

    //reset write and read position
    buf_in = 0;
    buf_out = 0;

    ult_t prod_thread;
    ult_t cons_threads[2];
    int cons_ids[2] = {1, 2};

    /* empty_slots (starts at 5)
    Producer WAITS on this : "Is there room to add?"
    Consumer POSTS to this : "I freed a slot!"

    full_slots (starts at 0)
    Consumer WAITS on this : "Is there anything to take?"
    Producer POSTS to this : "I added an item!"
     * */
    ult_create(&prod_thread, producer, NULL); //1 producer
    ult_create(&cons_threads[0], consumer, &cons_ids[0]); //consumer 1
    ult_create(&cons_threads[1], consumer, &cons_ids[1]);//consumer 2

    ult_join(prod_thread, NULL);//wait for producer
    ult_join(cons_threads[0], NULL);
    ult_join(cons_threads[1], NULL); // wait for consumers

    printf("[Main] Producer/Consumer test completed!\n");

    ult_sem_destroy(&empty_slots);
    ult_sem_destroy(&full_slots);
    ult_mutex_destroy(&buffer_mutex);
}

/* ============================================================
 * Test 5: Deadlock Detection
 * ============================================================ */

ult_mutex_t mutex_a;
ult_mutex_t mutex_b;
volatile int deadlock_ready = 0;

volatile int thread1_trying = 0;
volatile int thread2_trying = 0;

void *deadlock_thread_1(void *arg) {
    (void)arg;
    printf("[Deadlock Thread 1] Locking mutex A...\n");
    ult_mutex_lock(&mutex_a);
    printf("[Deadlock Thread 1] Got mutex A, waiting...\n");

    while (!deadlock_ready) {
        ult_yield();
    }

    printf("[Deadlock Thread 1] Now trying to lock mutex B...\n");
    thread1_trying = 1;           // signal BEFORE blocking
    ult_mutex_lock(&mutex_b);

    // never reaches here
    printf("[Deadlock Thread 1] Got both mutexes (unexpected)\n");
    ult_mutex_unlock(&mutex_b);
    ult_mutex_unlock(&mutex_a);
    return NULL;
}

void *deadlock_thread_2(void *arg) {
    (void)arg;
    printf("[Deadlock Thread 2] Locking mutex B...\n");
    ult_mutex_lock(&mutex_b);
    printf("[Deadlock Thread 2] Got mutex B\n");

    deadlock_ready = 1;

    // wait for thread 1 to be ready
    while (!thread1_trying) {
        ult_yield();
    }

    printf("[Deadlock Thread 2] Now trying to lock mutex A...\n");
    thread2_trying = 1;           // signal BEFORE blocking
    ult_mutex_lock(&mutex_a);

    // Never reaches here
    printf("[Deadlock Thread 2] Got both mutexes (unexpected)\n");
    ult_mutex_unlock(&mutex_a);
    ult_mutex_unlock(&mutex_b);
    return NULL;
}

void test_deadlock(void) {
    printf("\n========== TEST 5: Deadlock Detection ==========\n");
    printf("Creating a deadlock scenario...\n");

    ult_mutex_init(&mutex_a);
    ult_mutex_init(&mutex_b);
    deadlock_ready = 0;
    thread1_trying = 0;
    thread2_trying = 0;

    ult_t t1, t2;
    ult_create(&t1, deadlock_thread_1, NULL);
    ult_create(&t2, deadlock_thread_2, NULL);

    // Wait until BOTH threads are trying to get each other's mutex
    while (!thread1_trying || !thread2_trying) {
        ult_yield();
    }

    // Small extra yield to let them actually block
    for (int i = 0; i < 5; i++) {
        ult_yield();
    }

    printf("\n[Main] Auto-triggering deadlock detection...\n");
    ult_deadlock_detect();

    printf("\n[Main] Deadlock test complete. Press Ctrl+C to exit.\n");

    while(1) {
        ult_yield();  //keep yelding forever
    }
}
/* ============================================================
 * Test 6: Thread Self ID
 * ============================================================ */

void *self_test_thread(void *arg) {
    (void)arg;
    ult_t self = ult_self();
    printf("[Thread] My thread ID is: %d\n", self);
    return (void *)(long)self;
}

void test_self(void) {
    printf("\n========== TEST 6: Thread Self ID ==========\n");

    printf("[Main] Main thread ID: %d\n", ult_self());

    ult_t threads[3];
    for (int i = 0; i < 3; i++) {
        ult_create(&threads[i], self_test_thread, NULL);
    }

    for (int i = 0; i < 3; i++) {
        void *retval;
        ult_join(threads[i], &retval);
        printf("[Main] Thread reported its ID as: %ld\n", (long)retval);
    }
}

/* ============================================================
 * Main - Run all tests
 * ============================================================ */

int main(int argc, char *argv[]) {
    printf("==============================================\n");
    printf("  User-Level Thread Library Test Suite\n");
    printf("==============================================\n");

    /* Initialize the thread library */
    if (ult_init() != 0) {
        printf("Failed to initialize thread library!\n");
        return 1;
    }

    printf("[Main] Thread library initialized.\n");
    printf("[Main] Main thread ID: %d\n", ult_self());

    /* Check command line arguments for specific tests */
    int run_deadlock = 0;
    if (argc > 1) {
        if (argv[1][0] == 'd' || argv[1][0] == 'D') {
            run_deadlock = 1;
        }
    }

    if (run_deadlock) {
        /* Run only deadlock test */
        test_deadlock();
    } else {
        /* Run all tests except deadlock */
        test_basic_threads();
        test_self();
        test_preemption();
        test_mutex();
        test_semaphore();

        printf("\n==============================================\n");
        printf("  All tests completed successfully!\n");
        printf("==============================================\n");
        printf("\nTo test deadlock detection, run: ./test_ult d\n");
        printf("Then press Ctrl+\\ to see the deadlock report.\n");
    }

    /* Shutdown the library */
    ult_shutdown();

    return 0;
}
