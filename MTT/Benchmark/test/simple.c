#include <pthread.h>
int x = 1, y = 1, m = 0, n = 0;
bool check;
void* thr1(void * arg) { 
x = y + 1; 
m = y; 
x = 0; 
} 


void* thr2(void * arg) { 
y = x + 1; 
n = x; 
y = 0; 
} 


void main() {
	pthread_t t1, t2;
	pthread_create(&t1, 0, thr1, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_join(t1, 0);
	pthread_join(t2, 0);
	check = (m >= 2 && n != 3); 
}
