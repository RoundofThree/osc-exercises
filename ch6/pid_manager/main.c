#include <sys/types.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <limits.h>
#include "pid_manager.h"

void* thread_task(void* param);

// Number of bits in an unsigned
static const unsigned g_UNSIGNED_NBITS = sizeof(unsigned) * CHAR_BIT;
// Length of pid bitmap array
static const unsigned g_PID_ARR_LENGTH =
    (PID_RANGE + sizeof(unsigned)*CHAR_BIT - 1)/(sizeof(unsigned)*CHAR_BIT);
// PID table
static unsigned* g_pid_table;
// mutex lock 
pthread_mutex_t lock;

// Creates and initializes a data structure for representing pids.
int allocate_map(void) {
    g_pid_table = malloc(g_PID_ARR_LENGTH * sizeof(*g_pid_table));
    for (int i = 0; i < g_PID_ARR_LENGTH; i++) {
        g_pid_table[i] = 0;
    }
    return 1;
}

// Allocates a pid (toggles the respective bit) and returns the PID.
int allocate_pid(void) {
    pthread_mutex_lock(&lock);
    unsigned current_pid = MIN_PID;
    unsigned track_unsigned;
    // Iterate through unsigned entries
    for (int i = 0; i < g_PID_ARR_LENGTH; i++) {
        track_unsigned = g_pid_table[i];
        // Iterate over unsigned entry
        for (int j = 0; j < g_UNSIGNED_NBITS; j++) {
            if (current_pid > MAX_PID) {
                pthread_mutex_unlock(&lock);
                return 1;
            }
            if (!(track_unsigned & 1)) {
                // Set pid in pid manager bitmap
                g_pid_table[i] |= (1 << j);
                pthread_mutex_unlock(&lock);
                return current_pid;
            }
            current_pid += 1;
            track_unsigned >>= 1;
        }
    }
    pthread_mutex_unlock(&lock);
    return 1;
}

// Releases a pid by toggling the corresponding bitmap to 0.
void release_pid(int pid) {
    int pid_table_index;
    int pid_bit_num;
    unsigned mask_pid = ~0;
    // Check valid pid
    if (pid > MAX_PID || pid < MIN_PID)
        return;
    // Adjust pid to be 0 indexed
    pid -= MIN_PID;
    // Get the process bit number
    pid_bit_num = pid % g_UNSIGNED_NBITS;
    // Get which entry in the pid table
    pid_table_index = pid/g_UNSIGNED_NBITS;
    // Zero out appropriate pid bit number.
    mask_pid ^= (1 << pid_bit_num);
    g_pid_table[pid_table_index] &= mask_pid;
}

void print_map() {
    printf("Bitmap:\n");
    printf("Bitmap length: %x sizeof unsigned: %lu\n", g_PID_ARR_LENGTH, sizeof(unsigned));
    for (int i = 0; i < g_PID_ARR_LENGTH; i++) {
        unsigned current_entry = g_pid_table[i];
        if (current_entry)
            printf("%d: %x\n", i, g_pid_table[i]);
    }
    printf("\n");
}

void* thread_task(void* param) {
    int pid = allocate_pid();
    print_map();
    sleep(3);
    release_pid(pid);
    pthread_exit(0);
}

int main()
{
    int i;
    int num_threads = 1000;
    pthread_t threads[num_threads];
    if (pthread_mutex_init(&lock, NULL) != 0) {
        printf("Mutex init failed.\n");
        return 1;
    }
    allocate_map();

    for (i = 0; i < num_threads; i++) {
        if (pthread_create(&threads[i], NULL, thread_task, NULL) != 0) {
            printf("ERROR: Pthread created incorrectly.\n");
            exit(0);
        }
    }

    for (i = 0; i < num_threads; i++) {
        pthread_join(threads[i], NULL);
    }
    print_map();
    pthread_mutex_destroy(&lock);
    free(g_pid_table);
    return 0;
}