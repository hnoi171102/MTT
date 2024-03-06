int data1 = 10;
int data2 = 10;

void *t1()
{  
  data1 += 1;
  data2 += 1;
  return 0;
}


void *t2()
{  
  data1 += 5;
  data2 -= 6;
  return 0;
}


int stateful01_2()
{
  t1();
  t2();
  
  if (data1 != 16 && data2 != 5)
  {
    return 1;
  }
  else
  {
    return 0;
  }
}