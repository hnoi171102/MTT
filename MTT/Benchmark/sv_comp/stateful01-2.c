// This file is part of the SV-Benchmarks collection of verification tasks:
// https://github.com/sosy-lab/sv-benchmarks
//
// SPDX-FileCopyrightText: 2011-2020 The SV-Benchmarks community
// SPDX-FileCopyrightText: 2020 The ESBMC project
//
// SPDX-License-Identifier: Apache-2.0


#include <pthread.h>
int data1, data2;
bool check;
void * thread1(void * arg)
{  
  	data1 += 1;
  	data2 += 1;
  	return 0;
}
void * thread2(void * arg)
{  
  	data1+=5;
  	data2-=6;
  	return 0;
}
int main()
{
  	pthread_t  t1, t2;
  	data1 = 10;
  	data2 = 10;
  	pthread_create(&t1, 0, thread1, 0);
  	pthread_create(&t2, 0, thread2, 0);
  	pthread_join(t1, 0);
  	pthread_join(t2, 0);
	check = (data1!=16 && data2!=5);
  	return 0;
}


