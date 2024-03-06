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
  int i = 3, j = 6;

  int condI = (i >= 16);

  int condJ = (j >= 16);

  if (condI || condJ) {
    return 1;
  }

  return 0;
}