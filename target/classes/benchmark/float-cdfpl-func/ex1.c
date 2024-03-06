void *t1(void *arg) {
  for (int k = 0; k < NUM; k++) {
    i = j + 1;
  }
}

void *t2(void *arg) {
  for (int k = 0; k < NUM; k++) {
    j = i + 1;
  }
}

int main(int argc, char **argv) {
  int i = 3, j = 6;
  int NUM = 5;
  int LIMIT = 2*NUM+6;

  int condI = i >= LIMIT;

  int condJ = j >= LIMIT;

  if (condI || condJ) {
    return 1;
  }

  return 0;
}