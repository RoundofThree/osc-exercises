// this means (MxK) . (KxN)
#define M 3
#define K 3
#define N 2

#include <stdio.h>
#include <pthread.h>

int A[M][K] = {{1,2,3}, {1,4,6}, {1,9,6}};
int B[K][N] = {{3,5},{5,3}};
int C[M][N];

struct pos {
    int i;  // row
    int j;  // col 
};

int main() {

}