/* Testcase from Threader's distribution. For details see:
   http://www.model.in.tum.de/~popeea/research/threader
*/

#include <pthread.h>
int flag1 = 0, flag2 = 0; // boolean flags
int turn; // integer variable to hold the ID of the thread whose turn is it
int x; // boolean variable to test mutual exclusion
bool check; 
void *thr1(void *_) {
  flag1 = 1;
  turn = 1;
  check = (flag2 != 1 || turn != 1);
  x = 0;
  check = (x > 0);
  flag1 = 0;
  return 0;
}
void *thr2(void *_) {
  flag2 = 1;
  turn = 0;
  check = (flag1 != 1 || turn != 0);
  x = 1;
  check = (x < 1);
  flag2 = 0;
  return 0;
}
int main() {
  pthread_t t1, t2;
  pthread_create(&t1, 0, thr1, 0);
  pthread_create(&t2, 0, thr2, 0);
  pthread_join(t1, 0);
  pthread_join(t2, 0);
  return 0;
}

