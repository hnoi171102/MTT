// This file is part of the SV-Benchmarks collection of verification tasks:
// https://github.com/sosy-lab/sv-benchmarks
//
// SPDX-FileCopyrightText: 2011-2020 The SV-Benchmarks community
// SPDX-FileCopyrightText: 2018 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

#include <pthread.h>
int i = 3, j = 6;
int LIMIT = 16;
bool check;
void *t1(void *arg) {
	i = j + 1;
	i = j + 1;
	i = j + 1;
	i = j + 1;
	i = j + 1;
}
void *t2(void *arg) {
  	j = i + 1;
  	j = i + 1;
  	j = i + 1;
  	j = i + 1;
  	j = i + 1;
}
int main(int argc, char **argv) {
  	pthread_t id1, id2;
  	pthread_create(&id1, NULL, t1, NULL);
  	pthread_create(&id2, NULL, t2, NULL);
	check = (i >= LIMIT || j >= LIMIT)
  	return 0;
}

