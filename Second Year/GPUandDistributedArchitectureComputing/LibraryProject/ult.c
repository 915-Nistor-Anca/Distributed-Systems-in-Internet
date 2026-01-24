#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <sys/time.h>
#include <unistd.h>
#include "ult.h"
//static = private to this file only
static ult_tcb_t threads[ULT_MAX_THREADS];  // thread table
static ult_t current_tid = -1;               // currently running thread
static int num_threads = 0;                  // total threads created
static int initialized = 0;                  // library init flag
/*static ucontext_t scheduler_context;     */    // scheduler context
static sigset_t block_mask;                  // signal mask for blocking

// ready queue (simple circular array)
static ult_t ready_queue[ULT_MAX_THREADS];
static int ready_head = 0;
static int ready_tail = 0;
static int ready_count = 0;

// list of all mutexes for deadlock detection
static ult_mutex_t *all_mutexes[ULT_MAX_THREADS];
static int num_mutexes = 0;

// disable timer interrupts
static void block_signals(void) {
    sigprocmask(SIG_BLOCK, &block_mask, NULL); //ignore timer
}

//enable timer interrupts
static void unblock_signals(void) {
    sigprocmask(SIG_UNBLOCK, &block_mask, NULL);
}

// add thread to the back of ready queue
static void enqueue_ready(ult_t tid) {
    if (ready_count >= ULT_MAX_THREADS) return; // the queue is full
    ready_queue[ready_tail] = tid; // put thread at the tail
    ready_tail = (ready_tail + 1) % ULT_MAX_THREADS; //moves the tail
    ready_count++;
}

// remove thread from the front of ready queue
static ult_t dequeue_ready(void) {
    if (ready_count == 0) return -1; //nobody in line
    ult_t tid = ready_queue[ready_head]; // get thread at the front
    ready_head = (ready_head + 1) % ULT_MAX_THREADS; //move head
    ready_count--;
    return tid;
}

// remove specific thread from ready queue
static void remove_from_ready(ult_t tid) {
    ult_t temp[ULT_MAX_THREADS];
    int temp_count = 0;

    while (ready_count > 0) { // take everyone out, skip the one we want to remove
        ult_t t = dequeue_ready(); // take the thread at the front
        if (t != tid) { // if it s not the one we want to remove
            temp[temp_count++] = t; // add it to the temporary list
        }
    }

    for (int i = 0; i < temp_count; i++) { //put everyone back, except the removed one
        enqueue_ready(temp[i]);
    }
}

// find the next ready thread (so the one from head)
static ult_t find_next_thread(void) {
    return dequeue_ready();
}

// find empty spot for new thread
static ult_t allocate_tid(void) {
    for (int i = 0; i < ULT_MAX_THREADS; i++) {
        if (threads[i].state == ULT_STATE_UNUSED) { // search for an empty slot in the threads array
            return i;
        }
    }
    return -1;
}

// thread wrapper function
typedef struct {
    void *(*func)(void *); // the user function
    void *arg; // the argument to pass to it
} thread_args_t;

//makes sure ult_exit() is called
static void thread_wrapper(void) {
	ult_t tid = current_tid; //get the current thread id
    thread_args_t *args = (thread_args_t *)threads[tid].stack; //get the function and the arguments (in ult_create() we stored this info)

    void *(*func)(void *) = args->func;
    void *arg = args->arg;

    unblock_signals(); // the thread is set up, so timer interrupts can happen now

    // call the actual thread function
    void *retval = func(arg);

    // thread returned normally so exit
    ult_exit(retval);
}

//main scheduler which picks and switches threads
/*static void schedule(void) {
    block_signals(); //can't be interrupted by the timer

    ult_t prev_tid = current_tid; //save so remembers who was running
    ult_t next_tid;

    // find the next ready thread
    next_tid = find_next_thread();

    if (next_tid == -1) { // if there is nobody in line, check if we should keep current thread running
        int any_active = 0;
        for (int i = 0; i < ULT_MAX_THREADS; i++) { // check if any threads exist
            if (threads[i].state == ULT_STATE_RUNNING ||
                threads[i].state == ULT_STATE_READY ||
                threads[i].state == ULT_STATE_BLOCKED) {
                any_active = 1;
                break;
            }
        }
        if (!any_active) { //no threads active so nothing to do
            unblock_signals();
            return;
        }
       // current thread continues if it's the only one
        if (prev_tid >= 0 && threads[prev_tid].state == ULT_STATE_RUNNING) {
            unblock_signals();
            return;
        }
        unblock_signals();
        return;
    } // end of if so from now on the code is if we found a waiting thread

    //put the previous thread back in line if it was running
    if (prev_tid >= 0 && threads[prev_tid].state == ULT_STATE_RUNNING) {
        threads[prev_tid].state = ULT_STATE_READY;
        enqueue_ready(prev_tid);
    }

    current_tid = next_tid; // switch to the next thread
    threads[next_tid].state = ULT_STATE_RUNNING;

    unblock_signals(); //timer can interrupt again

    if (prev_tid >= 0) {
    	//saves everything about current thread, loads the next thread's saved state and jump where it left off
        swapcontext(&threads[prev_tid].context, &threads[next_tid].context);
    } else {
        setcontext(&threads[next_tid].context); //previous thread is gone so just load new (nothing to save)
    }
}*/

// called every 50ms by the operating system, forces thread switch (so them share the CPU)
//the function which actually switches the threads
static void timer_handler(int sig) {
    (void)sig;

    if (!initialized || current_tid < 0) return; //not initialized/ready yet
    if (threads[current_tid].state != ULT_STATE_RUNNING) return;

    // current thread's turn is over, back to the line
    threads[current_tid].state = ULT_STATE_READY;
    enqueue_ready(current_tid);

    // find next thread to run
    ult_t next_tid = find_next_thread();
    if (next_tid == -1) {
        // if there is nobody else, keep running the current thread
        threads[current_tid].state = ULT_STATE_RUNNING;
        remove_from_ready(current_tid);
        return;
    }

    //if there IS somebody, switch to it
    ult_t prev_tid = current_tid;
    current_tid = next_tid;
    threads[next_tid].state = ULT_STATE_RUNNING;

    //saves old context, loads new one
    swapcontext(&threads[prev_tid].context, &threads[next_tid].context);
}

// Ctrl+\ handler, shows deadlock info
static void sigquit_handler(int sig) {
    (void)sig;
    ult_deadlock_detect();
}

// start the preemptive (50ms repeating) timer
//start_timer rings the bell but the function who actually switches the threads is timer_handler
static void start_timer(void) {
    struct itimerval timer;
    timer.it_value.tv_sec = 0;
    timer.it_value.tv_usec = ULT_TIME_SLICE_US;// first alarm
    timer.it_interval.tv_sec = 0;
    timer.it_interval.tv_usec = ULT_TIME_SLICE_US; //repeat every x microseconds
    setitimer(ITIMER_REAL, &timer, NULL);
}

// stop the preemptive timer
static void stop_timer(void) {
    struct itimerval timer;
    memset(&timer, 0, sizeof(timer));
    setitimer(ITIMER_REAL, &timer, NULL);
}

//starts the library
int ult_init(void) {
    if (initialized) return 0; // if already initialized, do nothing

    //initialize thread list
    memset(threads, 0, sizeof(threads));
    for (int i = 0; i < ULT_MAX_THREADS; i++) { //clear all thread slots
        threads[i].state = ULT_STATE_UNUSED; // at first all of them are unused
        threads[i].tid = i; //thread id
        threads[i].join_tid = -1; // no one is waiting for them to finish
        threads[i].waiting_mutex = NULL;
        threads[i].waiting_sem = NULL; //doesn't wait for neither mutex or sem
    }

    // initialize ready queue
    ready_head = 0;
    ready_tail = 0;
    ready_count = 0;

    // thread 0 is the main thread: the one calling this function
    threads[0].state = ULT_STATE_RUNNING;
    threads[0].stack = NULL;  /* Main thread uses process stack */
    getcontext(&threads[0].context);
    current_tid = 0; // the thread id
    num_threads = 1; // only main thread

    // set up signal blocking for SIGALRM (timer)
    sigemptyset(&block_mask);
    sigaddset(&block_mask, SIGALRM);

    struct sigaction sa; // it's like a form: when catch SIGALRM, call timer_handler

    // timer handler for preemption
    memset(&sa, 0, sizeof(sa));
    sa.sa_handler = timer_handler; //the function which gets called, so in our case the one which switches threads
    sa.sa_flags = SA_RESTART;
    sigemptyset(&sa.sa_mask);
    sigaction(SIGALRM, &sa, NULL); //the signal SIGALRM with the function

    // SIGQUIT handler for deadlock detection
    memset(&sa, 0, sizeof(sa));
    sa.sa_handler = sigquit_handler; //different function this time, for deadlock
    sa.sa_flags = SA_RESTART;
    sigemptyset(&sa.sa_mask);
    sigaction(SIGQUIT, &sa, NULL); //when Ctrl+ happens, SIGQUIT => sigquit_handler

    // start automatic thread switching
    start_timer();

    initialized = 1;
    unblock_signals();
    return 0;
}

// library cleanup
void ult_shutdown(void) {
    if (!initialized) return;

    stop_timer();

    // free thread stacks
    for (int i = 0; i < ULT_MAX_THREADS; i++) {
        if (threads[i].stack != NULL) {
            free(threads[i].stack);
            threads[i].stack = NULL;
        }
        threads[i].state = ULT_STATE_UNUSED;
    }

    // reset signal handlers
    signal(SIGALRM, SIG_DFL);
    signal(SIGQUIT, SIG_DFL);

    current_tid = -1;
    num_threads = 0;
    initialized = 0;
    num_mutexes = 0;
}

int ult_create(ult_t *thread, void *(*start_routine)(void *), void *arg) {
    if (!initialized) {
        if (ult_init() != 0) return -1; // if not initialize, try to initialize
    }

    block_signals(); //do not interrupt while creating thread

    ult_t tid = allocate_tid();//find empty slot for new thread
    if (tid == -1) {
        unblock_signals();
        return -1;
    }

    //allocate stack
    void *stack = malloc(ULT_STACK_SIZE);
    if (stack == NULL) {
        unblock_signals();
        return -1;
    }

    // store thread args at base of stack
    thread_args_t *args = (thread_args_t *)stack;
    args->func = start_routine; // function
    args->arg = arg; // arguments

    // initialize thread
    threads[tid].tid = tid;
    threads[tid].state = ULT_STATE_READY; // ready to run
    threads[tid].stack = stack;
    threads[tid].retval = NULL;
    threads[tid].join_tid = -1;
    threads[tid].waiting_mutex = NULL;
    threads[tid].waiting_sem = NULL;

    // set up context (the context is a snapshot of the CPU state)
    // this tells the system where to start running and what stack to use
    getcontext(&threads[tid].context); // copies the current state of the CPU into threads[tid].context
    threads[tid].context.uc_stack.ss_sp = stack; // stack starts here
    threads[tid].context.uc_stack.ss_size = ULT_STACK_SIZE; //stack is this big
    threads[tid].context.uc_link = NULL; // defines what happens when context ends. null = don't automat. go anywhere/to other context
    makecontext(&threads[tid].context, thread_wrapper, 0); // (context to modify, function to start at, number of arguments)

    // add to ready queue
    enqueue_ready(tid);
    num_threads++;

    if (thread != NULL) {
        *thread = tid; //if the caller gave me a valid pointer, store the id there
    }

    unblock_signals();
    return 0;
}

//waits for a thread to finish
int ult_join(ult_t thread, void **retval) { // (the thread to wait for, where to store the threads return value)
    if (!initialized) return -1; // if library not initialized
    if (thread < 0 || thread >= ULT_MAX_THREADS) return -1; //if invalid thread id
    if (thread == current_tid) return -1;  // can't wait for yourself
    if (threads[thread].state == ULT_STATE_UNUSED) return -1; // if unused slot

    block_signals();

    // case 1: if thread already terminated, just get return value
    if (threads[thread].state == ULT_STATE_TERMINATED) {
        if (retval != NULL) {
            *retval = threads[thread].retval; // get return value
        }
        // clean up the finished thread
        if (threads[thread].stack != NULL) {
            free(threads[thread].stack);
            threads[thread].stack = NULL;
        }
        threads[thread].state = ULT_STATE_UNUSED;
        unblock_signals();
        return 0;
    }

    // case 2: someone else is already waiting for this thread
    if (threads[thread].join_tid != -1) {
        unblock_signals();
        return -1; // only one waiter allowed
    }

    // case 3: thread still running, we need to wait (we'll block ourselves and wait)
    threads[thread].join_tid = current_tid; // tell thread who is waiting
    threads[current_tid].state = ULT_STATE_BLOCKED; // we are now blocked

    // switch to another thread while we wait (we want to block, but someone MUST run!)
    // one thread runs at the time. one cpu. if we block ourselves, who runs the cpu?
    ult_t next_tid = find_next_thread();
    if (next_tid == -1) { // if nobody else exists...
        // cant block if nobody else can run! => undo everything and fail
        threads[current_tid].state = ULT_STATE_RUNNING; //from blocked to running
        threads[thread].join_tid = -1; // i m not waiting anymore
        unblock_signals();
        return -1; // join/wait failed
    }

    //if someone else exists, switch to them!
    ult_t prev_tid = current_tid;
    current_tid = next_tid;
    threads[next_tid].state = ULT_STATE_RUNNING;

    unblock_signals();
    swapcontext(&threads[prev_tid].context, &threads[next_tid].context); //one that one thread while I sleep/wait
    // swap context saves the exact position, including the line i'm at so when someone switches back to me, i resume right where i left off
    // we wake up here
    block_signals();

    if (retval != NULL) {
        *retval = threads[thread].retval; //get the return value
    }

    // clean up finished thread ( the one that gave us the value)
    if (threads[thread].stack != NULL) {
        free(threads[thread].stack);
        threads[thread].stack = NULL;
    }
    threads[thread].state = ULT_STATE_UNUSED;

    unblock_signals();
    return 0;
}

//ends a current thread
void ult_exit(void *retval) {
    if (!initialized || current_tid < 0) {
        exit(0);
    }

    block_signals();

    ult_t tid = current_tid;
    threads[tid].retval = retval;
    threads[tid].state = ULT_STATE_TERMINATED;

    // wake up any thread waiting to join
    if (threads[tid].join_tid != -1) {
        ult_t join_tid = threads[tid].join_tid;
        threads[join_tid].state = ULT_STATE_READY;
        enqueue_ready(join_tid);
    }

    // if main thread is the one who exits, shut down everything
    if (tid == 0) {
        unblock_signals();
        ult_shutdown();
        exit(0);
    }

    // find next thread to run
    ult_t next_tid = find_next_thread();

    if (next_tid == -1) {
        // no more threads - shut down
        unblock_signals();
        ult_shutdown();
        exit(0);
    }

    // switch to the next thread
    current_tid = next_tid;
    threads[next_tid].state = ULT_STATE_RUNNING;

    unblock_signals();
    setcontext(&threads[next_tid].context); //jump to the next thread, no return (that s why setcontext, not make context)
}

ult_t ult_self(void) {
    return current_tid;
}

//lets a thread voluntarily give up the CPU and let others run
void ult_yield(void) {
    if (!initialized || current_tid < 0) return;

    block_signals();

    // put current thread back in ready queue
    threads[current_tid].state = ULT_STATE_READY;
    enqueue_ready(current_tid);

    // find next thread
    ult_t next_tid = find_next_thread();

    if (next_tid == -1 || next_tid == current_tid) {
        // no other thread ready? then keep running :)
        threads[current_tid].state = ULT_STATE_RUNNING;
        remove_from_ready(current_tid);
        unblock_signals();
        return;
    }

    //switch to next thread
    ult_t prev_tid = current_tid;
    current_tid = next_tid;
    threads[next_tid].state = ULT_STATE_RUNNING;

    unblock_signals();
    swapcontext(&threads[prev_tid].context, &threads[next_tid].context);
}


int ult_mutex_init(ult_mutex_t *mutex) {
    if (mutex == NULL) return -1; // did the caller gave a valid pointer

    block_signals();

    mutex->locked = 0; //not locked yet
    mutex->owner = -1; //nobody owns it
    mutex->wait_count = 0; //nobody waiting
    memset(mutex->wait_queue, 0, sizeof(mutex->wait_queue));
    mutex->initialized = 1;

    // track mutex for deadlock detection (used in ult_deadlock_detect())
    //when i press ctr+\, it checks mutexes: who owns each one, whos waiting for each one, is there a cycle
    if (num_mutexes < ULT_MAX_THREADS) {
        all_mutexes[num_mutexes++] = mutex;
    }

    unblock_signals();
    return 0;
}

int ult_mutex_destroy(ult_mutex_t *mutex) {
    if (mutex == NULL || !mutex->initialized) return -1;

    block_signals();

    // can't destroy if threads are waiting
    if (mutex->wait_count > 0) {
        unblock_signals();
        return -1;
    }

    // remove from tracking
    for (int i = 0; i < num_mutexes; i++) {
        if (all_mutexes[i] == mutex) {
            all_mutexes[i] = all_mutexes[--num_mutexes];
            break;
        }
    }

    //uninitialize
    mutex->initialized = 0;
    mutex->locked = 0;
    mutex->owner = -1;

    unblock_signals();
    return 0;
}

int ult_mutex_lock(ult_mutex_t *mutex) {
    if (mutex == NULL || !mutex->initialized) return -1;
    if (!initialized) return -1;

    block_signals();

    // if already own the mutex, that s an error because it would deadlock
    if (mutex->locked && mutex->owner == current_tid) {
        unblock_signals();
        return -1;
    }

    // if it's not locked, grab it :)
    if (!mutex->locked) {
        mutex->locked = 1;
        mutex->owner = current_tid;
        unblock_signals();
        return 0;
    }

    // mutex is locked - add to wait queue and block (we have to wait)
    mutex->wait_queue[mutex->wait_count++] = current_tid;
    threads[current_tid].state = ULT_STATE_BLOCKED;
    threads[current_tid].waiting_mutex = mutex; // remember what we re waiting for

    // find next thread to run
    ult_t next_tid = find_next_thread();

    if (next_tid == -1) {
        // no runnable thread - deadlock detected so undo and fail
        mutex->wait_count--;
        threads[current_tid].state = ULT_STATE_RUNNING;
        threads[current_tid].waiting_mutex = NULL;
        unblock_signals();
        return -1;
    }

    ult_t prev_tid = current_tid;
    current_tid = next_tid;
    threads[next_tid].state = ULT_STATE_RUNNING;

    unblock_signals();
    swapcontext(&threads[prev_tid].context, &threads[next_tid].context);

    // we've been woken up - we now own the mutex :)
    threads[prev_tid].waiting_mutex = NULL;
    return 0;
}

int ult_mutex_unlock(ult_mutex_t *mutex) {
    if (mutex == NULL || !mutex->initialized) return -1;
    if (!mutex->locked) return -1;
    if (mutex->owner != current_tid) return -1;  // i am not the owner

    block_signals();

    if (mutex->wait_count > 0) {
        // wake up first waiting thread
        ult_t wake_tid = mutex->wait_queue[0];

        // shift queue
        for (int i = 0; i < mutex->wait_count - 1; i++) {
            mutex->wait_queue[i] = mutex->wait_queue[i + 1];
        }
        mutex->wait_count--;

        // transfer ownership
        mutex->owner = wake_tid;
        threads[wake_tid].state = ULT_STATE_READY;
        threads[wake_tid].waiting_mutex = NULL;
        enqueue_ready(wake_tid);
    } else {
        // no waiters so just unlock
        mutex->locked = 0;
        mutex->owner = -1;
    }

    unblock_signals();
    return 0;
}

int ult_sem_init(ult_sem_t *sem, int value) {
    if (sem == NULL || value < 0) return -1;

    block_signals();

    sem->value = value; // initial count = no of permits available
    sem->wait_count = 0;
    memset(sem->wait_queue, 0, sizeof(sem->wait_queue)); //clear the wait queue
    sem->initialized = 1; /// mark as initialized

    unblock_signals();
    return 0;
}

int ult_sem_destroy(ult_sem_t *sem) {
    if (sem == NULL || !sem->initialized) return -1;

    block_signals();

    // can't destroy if threads are waiting
    if (sem->wait_count > 0) {
        unblock_signals();
        return -1;
    }

    sem->initialized = 0;
    sem->value = 0;

    unblock_signals();
    return 0;
}

// wait (decrement) on a semaphore.
int ult_sem_wait(ult_sem_t *sem) {
    if (sem == NULL || !sem->initialized) return -1;
    if (!initialized) return -1;

    block_signals();

    sem->value--;

    if (sem->value >= 0) {
        // resource available :)
        unblock_signals();
        return 0;
    }

    //must wait - add to queue and block
    sem->wait_queue[sem->wait_count++] = current_tid;
    threads[current_tid].state = ULT_STATE_BLOCKED;
    threads[current_tid].waiting_sem = sem;

    //find next thread
    ult_t next_tid = find_next_thread();

    if (next_tid == -1) {
        // no runnable thread so start running again and fail
        sem->value++;
        sem->wait_count--;
        threads[current_tid].state = ULT_STATE_RUNNING;
        threads[current_tid].waiting_sem = NULL;
        unblock_signals();
        return -1;
    }

    ult_t prev_tid = current_tid;
    current_tid = next_tid;
    threads[next_tid].state = ULT_STATE_RUNNING; // found another thread who will run

    unblock_signals();
    swapcontext(&threads[prev_tid].context, &threads[next_tid].context);

    // woken up
    threads[prev_tid].waiting_sem = NULL;
    return 0;
}

// increments a semaphore and wakes up a thread if any
int ult_sem_post(ult_sem_t *sem) {
    if (sem == NULL || !sem->initialized) return -1; //if it's not initialized

    block_signals(); //don't want to be interrupted

    sem->value++; //increment semaphore value

    if (sem->wait_count > 0) { // if there are threads who wait
        ult_t wake_tid = sem->wait_queue[0]; // wake up the first thread

        // shift queue
        for (int i = 0; i < sem->wait_count - 1; i++) {
            sem->wait_queue[i] = sem->wait_queue[i + 1];
        }
        sem->wait_count--;

        threads[wake_tid].state = ULT_STATE_READY; // the woken thread is now ready
        threads[wake_tid].waiting_sem = NULL; //the thread does not wait for any semaphore now
        enqueue_ready(wake_tid); //add it to the queue
    }

    unblock_signals();
    return 0;
}

// DEADLOCK detection arrays
static int visited[ULT_MAX_THREADS]; //have we visited this thread?
static int rec_stack[ULT_MAX_THREADS]; // is this thread in the current path?
static int deadlock_threads[ULT_MAX_THREADS]; //threads involved in deadlock
static int deadlock_count;

// who is this thread waiting for
static ult_t get_waiting_for(ult_t tid) {
    if (tid < 0 || tid >= ULT_MAX_THREADS) return -1; // check if the thread is valid
    if (threads[tid].state != ULT_STATE_BLOCKED) return -1; //check if the thread is really blocked

    ult_mutex_t *mutex = threads[tid].waiting_mutex;
    if (mutex != NULL && mutex->locked) { //if the thread is waiting for a mutex and the mutex is locked, return the mutex owner
        return mutex->owner;
    }
    //we don't wait for semaphore because semaphore has no owner
    return -1;
}

// looks for circular waiting (deadlocks) in depth first search
static int dfs_cycle(ult_t tid) {
    if (tid < 0 || tid >= ULT_MAX_THREADS) return 0; // if the thread is invalid
    if (threads[tid].state == ULT_STATE_UNUSED) return 0; // or if it's unused, it can't be a cycle

    visited[tid] = 1; //mark as visited
    rec_stack[tid] = 1; //add to current path

    ult_t waiting_for = get_waiting_for(tid); // find out who this thread is waiting for

    if (waiting_for >= 0) { // if it waits for some thread
    	//case 1: if the thread we are waiting for wasnt visited, keep exploring
        if (!visited[waiting_for] && dfs_cycle(waiting_for)) {
            deadlock_threads[deadlock_count++] = tid; // add it to the list of deadlock threads
            return 1;
        } else if (rec_stack[waiting_for]) { // the thread we are waiting for is already in the path: deadlock :(
            deadlock_threads[deadlock_count++] = tid;
            return 1;
        }
    }

    //this means the thread is waiting for nobody. we are done exploring this stack and no cycle was found
    rec_stack[tid] = 0;
    return 0;
}

void ult_deadlock_detect(void) {
    printf("\n========== DEADLOCK DETECTION REPORT ==========\n");

    if (!initialized) { // the library was not initialized
        printf("Thread library not initialized.\n");
        printf("===============================================\n\n");
        return;
    }

    // reset the arrays for the algorithm
    memset(visited, 0, sizeof(visited));
    memset(rec_stack, 0, sizeof(rec_stack));
    deadlock_count = 0;

    // print thread states
    printf("\nThread States:\n");
    printf("%-6s %-12s %-20s\n", "TID", "State", "Waiting For"); // it will look like a table with those columns
    printf("----------------------------------------------\n");

    for (int i = 0; i < ULT_MAX_THREADS; i++) {
        if (threads[i].state == ULT_STATE_UNUSED) continue;

        const char *state_str;
        switch (threads[i].state) {
            case ULT_STATE_READY:      state_str = "READY"; break;
            case ULT_STATE_RUNNING:    state_str = "RUNNING"; break;
            case ULT_STATE_BLOCKED:    state_str = "BLOCKED"; break;
            case ULT_STATE_TERMINATED: state_str = "TERMINATED"; break;
            default:                   state_str = "UNKNOWN"; break;
        }

        char waiting_str[64] = "None"; //determine what they are waiting for
        if (threads[i].waiting_mutex != NULL) { //if it's waiting for a mutex
            ult_t owner = threads[i].waiting_mutex->owner; //take the mutex owner
            snprintf(waiting_str, sizeof(waiting_str), "Mutex (owner: %d)", owner); // print the owner
        } else if (threads[i].waiting_sem != NULL) { // if it's waiting for a semaphore
            snprintf(waiting_str, sizeof(waiting_str), "Semaphore");
        } else if (threads[i].join_tid != -1) { //if it's waiting for another thread to finish
            snprintf(waiting_str, sizeof(waiting_str), "Join (thread %d)", threads[i].join_tid);
        }

        printf("%-6d %-12s %-20s\n", i, state_str, waiting_str);
    }

    // check for cycles (mutex-based deadlocks)
    printf("\nDeadlock Analysis:\n");

    int found_deadlock = 0;
    for (int i = 0; i < ULT_MAX_THREADS; i++) { // try every thread as the starting point
        if (threads[i].state != ULT_STATE_UNUSED && !visited[i]) { //skip if already checked or it doesn't exist
            if (dfs_cycle(i)) { //returns one if cycle found
                found_deadlock = 1;
            }
        }
    }

    if (found_deadlock && deadlock_count > 0) { // if a deadlock was found
        printf("  DEADLOCK DETECTED!\n");
        printf("  Threads involved in deadlock cycle:\n");
        for (int i = 0; i < deadlock_count; i++) { // take each thread from deadlock
            ult_t tid = deadlock_threads[i];
            printf("    - Thread %d waiting for ", tid); // the thread which is waiting
            if (threads[tid].waiting_mutex != NULL) {
                printf("mutex owned by thread %d\n", threads[tid].waiting_mutex->owner); // the other thread which has the mutex
            } else {
                printf("unknown resource\n");
            }
        }
    } else {
        printf("  No deadlock detected.\n");
    }

    // print mutex states
    printf("\nMutex States:\n");
    if (num_mutexes == 0) {
        printf("  No mutexes registered.\n");
    } else {
        for (int i = 0; i < num_mutexes; i++) {
            ult_mutex_t *m = all_mutexes[i]; // take each mutex
            if (m == NULL || !m->initialized) continue; //if it's not initialized or null

            printf("  Mutex %d: %s", i, m->locked ? "LOCKED" : "UNLOCKED"); //mutex id and if it's locked/unlocked
            if (m->locked) {
                printf(" (owner: thread %d)", m->owner); //the owner if it's locked
            }
            if (m->wait_count > 0) { // if there are threads who are waiting
                printf(" [waiting: ");
                for (int j = 0; j < m->wait_count; j++) { // print the threads who are waiting for this mutex
                    printf("%d", m->wait_queue[j]);
                    if (j < m->wait_count - 1) printf(", "); // only for printing ","
                }
                printf("]");
            }
            printf("\n");
        }
    }

    printf("===============================================\n\n");
}
