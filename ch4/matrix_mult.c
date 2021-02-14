// this means (MxK) . (KxN)
#define M 3
#define K 3
#define N 2

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
// matrices are statically stored in code
// may use fscanf(FILE* file, "%d", &n) in the future
// FILE* file = fopen("matrices.txt", "r"); 

// use gcc -pthread -o test matrix_mult.c
int A[M][K] = {{1,2,3}, {1,4,6}, {1,9,6}};
int B[K][N] = {{3,5},{5,3}};
int C[M][N];

typedef struct {
    int i;  // row
    int j;  // col 
} pos;

void* runner(void* params);

int main() {
    pthread_t tids[M*N];
    pthread_attr_t attr;
    pthread_attr_init(&attr);
    for (int r=0; r<M; ++r) {
        for (int c=0; c<N; ++c) {
            pos* position;
            position = malloc(sizeof(pos));
            position->i = r;
            position->j = c;
            pthread_create(&tids[r*N+c], &attr, runner, position);
        }
    }
    // wait for all threads
    for (int i=0; i<M*N; ++i) {
        pthread_join(tids[i], NULL);
    }
    // print the result
    for (int i=0; i<M; ++i) {
        for (int j=0; j<N; ++j) {
            printf("%d ", C[i][j]);
        }
        printf("\n");
    }
    return 0;
}

void* runner(void* params) {
    pos* p = params;
    int r = p->i;
    int c = p->j;
    int ret = 0;
    for (int i=0; i<K; ++i) {
        ret += A[r][i] * B[i][c];
    }
    C[r][c] = ret;
    pthread_exit(0);
}