int a = 0;
int b = 0;
int check = -1;

void t1()
{
  a = 1;
  b = -1;
}

void t2()
{
  if ((a == 0 && b == 0) || (a == 1 && b == -1) || true)
  {
    check = 1;
  }

}

int reorder_c11_good_30()
{
  t1();
  t1();
  t1();
  t1();
  t1();
  t1();
  t1();
  t1();
  t1();
  t1();

  t1();
  t1();
  t1();
  t1();
  t1();
  t1();
  t1();
  t1();
  t1();
  t1();

  t1();
  t1();
  t1();
  t1();
  t1();
  t1();
  t1();
  t1();
  t1();
  t1();
  t2();
  if (check == 1) {
    return 1;
  }
  else
  {
    return 0;
  }
}
