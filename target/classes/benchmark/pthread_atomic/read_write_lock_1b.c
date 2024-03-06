int w = 0;
int r = 0;
int x;
int y;
bool check = false;

void t1()
{
  check = (w != 0 || r != 0)
  w = 1;
  x = 3;
  w = 0;
}

void t2()
{
  int l;
  check = (w != 0)
  r = r+1;
  l = x;
  y = l;
  check = (y != x);
  r = r - 1;
}

void t3()
{
  check = (w != 0 || r != 0)
  w = 1;
  x = 3;
  w = 0;
}

void t4()
{
  int l;
  check = (w != 0)
  r = r+1;
  l = x;
  y = l;
  check = (y != x);
  r = r - 1;
}

int read_write_lock_1b()
{
  t1();
  t2();
  t3();
  t4();
  if (check == true) {
    return 1;
  }
  else
  {
    return 0;
  }
}
