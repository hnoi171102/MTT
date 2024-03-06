// This file is part of the SV-Benchmarks collection of verification tasks:
// https://github.com/sosy-lab/sv-benchmarks
//
// SPDX-FileCopyrightText: 2011-2020 The SV-Benchmarks community
// SPDX-FileCopyrightText: 2020 The ESBMC project
//
// SPDX-License-Identifier: Apache-2.0


#include <pthread.h>
int x;
int n = 20;
bool check;
void* thr1(void* arg) {
    check = (x > n);
}
void* thr2(void* arg) {
    int t;
    t = x;
    x = t + 1;
}
int main(int argc, char* argv[]) {
    pthread_t t1, t2;
    x = 0;
	pthread_create(&t1, 0, thr1, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0); 
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0); 
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);     
    return 0;
}

