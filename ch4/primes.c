#include <stdio.h>
#include <pthread.h>
#include <math.h>

// gcc primes.c -pthread -lm

void *primes(void *param){
	int n=*((int *)param);
	for(int j=2;j<=n;j++){
		int b=0;
		int s=(int)sqrt(j);
		for(int i=2;i<=s;i++){
			if(j%i==0){
				b=1;
				break;
			}
		}
		if(b==0)
			printf("%d ",j);
	}
	printf("\n");
	pthread_exit(0);
}

int main() {
    int n;
    scanf("%d", &n);
    pthread_t tid;
    pthread_attr_t attr; 
    pthread_attr_init(&attr);
    pthread_create(&tid, &attr, primes, &n);
    pthread_join(tid, NULL);
    return 0;
}