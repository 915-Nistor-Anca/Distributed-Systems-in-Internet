#ifndef ULT_H
#define ULT_H

#include <ucontext.h>
#include <stddef.h>

#define ULT_MAX_THREADS     64
#define ULT_STACK_SIZE      (64 * 1024)
#define ULT_TIME_SLICE_US   10000 // 0.05seconds - how long a thread runs before switching

// thread states
typedef enum {
    ULT_STATE_UNUSED = 0, //slot is empty, can create a new thread here
    ULT_STATE_READY, // waiting in line for CPU
    ULT_STATE_RUNNING, //curently executing
    ULT_STATE_BLOCKED, //waiting for something (mutex, semaphore, join)
    ULT_STATE_TERMINATED //finished and waiting to be cleaned up
} ult_state_t;

// thread id type
typedef int ult_t;

// forward declarations
struct ult_mutex;
struct ult_sem;

// thread control block - id card for each thread with all its info
typedef struct ult_tcb {
    ult_t           tid;                    //thread id
    ult_state_t     state;                  // curent state
    ucontext_t      context;                // CPU state snapshot
    void           *stack;                  // stack memory
    void           *retval;                 //return value when thread finishes
    ult_t           join_tid;               // who is waiting for this thread to finish (-1 if nobody)
    struct ult_mutex *waiting_mutex;        // which mutex is this thread waiting for (null if none)
    struct ult_sem   *waiting_sem;          // which semaphore is this thread waiting for (null if none)
} ult_tcb_t;

// Mutex structure
typedef struct ult_mutex {
    int             locked;                 // 0 if unlocked, 1 if locked
    ult_t           owner;                  // what thread owns it (-1 if nobody)
    ult_t           wait_queue[ULT_MAX_THREADS]; //threads waiting to acquire this lock
    int             wait_count;             // how many threads are waiting
    int             initialized;            // if mutex_init has been called
} ult_mutex_t;

// Semaphore structure
typedef struct ult_sem {
    int             value;                  // counter (how many permits available)
    ult_t           wait_queue[ULT_MAX_THREADS]; // threads waiting for a permit
    int             wait_count;             // how many threads are waiting
    int             initialized;            // if sem_init has been called
} ult_sem_t;

/*
 * Initialize the thread library.
 * Must be called before any other ult_* functions.
 * Returns 0 on success, -1 on failure.
 */
int ult_init(void);

/*
 * Shutdown the thread library.
 * Cleans up all resources.
 */
void ult_shutdown(void);

/*
 * Create a new thread.
 *
 * Parameters:
 *   thread - Pointer to store the new thread ID
 *   start_routine - Function to execute in the new thread
 *   arg - Argument to pass to start_routine
 *
 * Returns 0 on success, -1 on failure.
 */
int ult_create(ult_t *thread, void *(*start_routine)(void *), void *arg);

/*
 * Wait for a thread to finish.
 *
 * Parameters:
 *   thread - Thread ID to wait for
 *   retval - Pointer to store the thread's return value (can be NULL)
 *
 * Returns 0 on success, -1 on failure.
 */
int ult_join(ult_t thread, void **retval);

/*
 * End the current thread.
 *
 * Parameters:
 *   retval - Return value to pass to joining thread
 */
void ult_exit(void *retval);

/*
 * Get the thread ID of the current thread.
 *
 * Returns the current thread's ID.
 */
ult_t ult_self(void);

/*
 * Voluntarily yield the CPU to another thread. So it lets another thread run
 */
void ult_yield(void);

/*
 * Initialize a mutex.
 *
 * Parameters:
 *   mutex - Pointer to mutex to initialize
 *
 * Returns 0 on success, -1 on failure.
 */
int ult_mutex_init(ult_mutex_t *mutex);

/*
 * Destroy a mutex.
 *
 * Parameters:
 *   mutex - Pointer to mutex to destroy
 *
 * Returns 0 on success, -1 on failure.
 */
int ult_mutex_destroy(ult_mutex_t *mutex);

/*
 * Lock a mutex.
 * Blocks if the mutex is already locked.
 *
 * Parameters:
 *   mutex - Pointer to mutex to lock
 *
 * Returns 0 on success, -1 on failure.
 */
int ult_mutex_lock(ult_mutex_t *mutex);

/*
 * Unlock a mutex.
 * Must be called by the thread that locked the mutex.
 *
 * Parameters:
 *   mutex - Pointer to mutex to unlock
 *
 * Returns 0 on success, -1 on failure.
 */
int ult_mutex_unlock(ult_mutex_t *mutex);

/*
 * Initialize a semaphore.
 *
 * Parameters:
 *   sem - Pointer to semaphore to initialize
 *   value - Initial value of the semaphore
 *
 * Returns 0 on success, -1 on failure.
 */
int ult_sem_init(ult_sem_t *sem, int value);

/*
 * Destroy a semaphore.
 *
 * Parameters:
 *   sem - Pointer to semaphore to destroy
 *
 * Returns 0 on success, -1 on failure.
 */
int ult_sem_destroy(ult_sem_t *sem);

/*
 * Wait (decrement) on a semaphore.
 * Blocks if the semaphore value is zero.
 *
 * Parameters:
 *   sem - Pointer to semaphore
 *
 * Returns 0 on success, -1 on failure.
 */
int ult_sem_wait(ult_sem_t *sem);

/*
 * Post (increment) on a semaphore.
 * Wakes up a waiting thread if any.
 *
 * Parameters:
 *   sem - Pointer to semaphore
 *
 * Returns 0 on success, -1 on failure.
 */
int ult_sem_post(ult_sem_t *sem);

/*
 * Check for deadlocks and print a report.
 * Called automatically on SIGQUIT (Ctrl+\).
 * Can also be called manually.
 */
void ult_deadlock_detect(void);

#endif /* ULT_H */
