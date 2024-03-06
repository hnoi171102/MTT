// This file is part of the SV-Benchmarks collection of verification tasks:
// https://github.com/sosy-lab/sv-benchmarks
//
// SPDX-FileCopyrightText: 2016 SCTBench Project
// SPDX-FileCopyrightText: The ESBMC Project
//
// SPDX-License-Identifier: Apache-2.0
#include <pthread.h>

int a = 0, b = 0;
bool check;

void *setThread(void *param) {
  a = 1;
  b = -1;
  return NULL;
}

void *checkThread(void *param) {
  //assert((a == 0 && b == 0) || (a == 1 && b == -1) || 1);
  check = ((a != 0 || b != 0) && (a != 1 || b != -1) && false);
  return NULL;
}

int main() {
	pthread_t set1, set2, set3, set4, set5, set6, set7, set8, set9, set10;
	pthread_t check1;
	
	pthread_create(&set1, NULL, setThread, NULL);
	pthread_create(&set2, NULL, setThread, NULL);
	pthread_create(&set3, NULL, setThread, NULL);
	pthread_create(&set4, NULL, setThread, NULL);
	pthread_create(&set5, NULL, setThread, NULL);
	pthread_create(&set6, NULL, setThread, NULL);
	pthread_create(&set7, NULL, setThread, NULL);
	pthread_create(&set8, NULL, setThread, NULL);
	pthread_create(&set9, NULL, setThread, NULL);
	pthread_create(&set10, NULL, setThread, NULL);
	
	pthread_create(&check1, NULL, checkThread, NULL);
	
	pthread_join(set1, NULL);
	pthread_join(set2, NULL);
	pthread_join(set3, NULL);
	pthread_join(set4, NULL);
	pthread_join(set5, NULL);
	pthread_join(set6, NULL);
	pthread_join(set7, NULL);
	pthread_join(set8, NULL);
	pthread_join(set9, NULL);
	pthread_join(set10, NULL);
	
	pthread_join(check1, NULL);
	return 0;
}

