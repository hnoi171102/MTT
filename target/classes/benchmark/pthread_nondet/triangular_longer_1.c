int i = 3;
int j = 6;
void *t1()
{
  i = j + 1;
  i = j + 1;
  i = j + 1;
  i = j + 1;
  i = j + 1;
  i = j + 1;
  i = j + 1;
  i = j + 1;
  i = j + 1;
  i = j + 1;
}

void *t2()
{
  j = i + 1;
  j = i + 1;
  j = i + 1;
  j = i + 1;
  j = i + 1;
  j = i + 1;
  j = i + 1;
  j = i + 1;
  j = i + 1;
  j = i + 1;
}

int triangular_longer_1()
{
  t1();
  t2();
  if (i > 26 || j > 26)
  {
    return 1;
  }
  else
  {
    return 0;
  }
}