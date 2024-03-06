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
  check = ((a != 0 || b != 0) && (a != 1 || b != -1));
  return NULL;
}
int main() {
	pthread_t set1, set2, set3, set4, set5, set6, set7, set8, set9, set10;
	pthread_t set11, set12, set13, set14, set15, set16, set17, set18, set19, set20;
	pthread_t set21, set22, set23, set24, set25, set26, set27, set28, set29, set30;
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
	pthread_create(&set11, NULL, setThread, NULL);
	pthread_create(&set12, NULL, setThread, NULL);
	pthread_create(&set13, NULL, setThread, NULL);
	pthread_create(&set14, NULL, setThread, NULL);
	pthread_create(&set15, NULL, setThread, NULL);
	pthread_create(&set16, NULL, setThread, NULL);
	pthread_create(&set17, NULL, setThread, NULL);
	pthread_create(&set18, NULL, setThread, NULL);
	pthread_create(&set19, NULL, setThread, NULL);
	pthread_create(&set20, NULL, setThread, NULL);
	pthread_create(&set21, NULL, setThread, NULL);
	pthread_create(&set22, NULL, setThread, NULL);
	pthread_create(&set23, NULL, setThread, NULL);
	pthread_create(&set24, NULL, setThread, NULL);
	pthread_create(&set25, NULL, setThread, NULL);
	pthread_create(&set26, NULL, setThread, NULL);
	pthread_create(&set27, NULL, setThread, NULL);
	pthread_create(&set28, NULL, setThread, NULL);
	pthread_create(&set29, NULL, setThread, NULL);
	pthread_create(&set30, NULL, setThread, NULL);
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
	pthread_join(set11, NULL);
	pthread_join(set12, NULL);
	pthread_join(set13, NULL);
	pthread_join(set14, NULL);
	pthread_join(set15, NULL);
	pthread_join(set16, NULL);
	pthread_join(set17, NULL);
	pthread_join(set18, NULL);
	pthread_join(set19, NULL);
	pthread_join(set20, NULL);
	pthread_join(set21, NULL);
	pthread_join(set22, NULL);
	pthread_join(set23, NULL);
	pthread_join(set24, NULL);
	pthread_join(set25, NULL);
	pthread_join(set26, NULL);
	pthread_join(set27, NULL);
	pthread_join(set28, NULL);
	pthread_join(set29, NULL);
	pthread_join(set30, NULL);
	pthread_join(check1, NULL);
	return 0;
}

