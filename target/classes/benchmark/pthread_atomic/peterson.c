int flag1 = 0;
int flag2 = 0;
int turn;
int x;
bool check = false;

void t1() {
  flag1 = 1;
  turn = 1;
  check = (flag2 != 1 || turn != 1);
  x = 0;
  check = (x > 0);
  flag1 = 0;
  return 0;
}

void t2() {
  flag2 = 1;
  turn = 0;
  check = (flag1 != 1 || turn != 0);
  x = 1;
  check = (x < 1);
  flag2 = 0;
  return 0;
}

int peterson()
{
  t1();
  t2();
  if (check == true) {
    return 1;
  }
  else
  {
    return 0;
  }
}
