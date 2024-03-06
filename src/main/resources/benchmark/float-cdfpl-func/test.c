int x = 1;
int y = 1;

void* thr1()
{
    y = x + 1;
    x = y;
    y = x * 3;
}

void* thr2()
{
    x = y + 1;
    y = x * 4;
}

void* thr3()
{
    y = x + 1;
    x = y + 2;
}

int test() {
    thr1();
    thr2();
    thr3();
    if(x == 3 && y == 3)
    {
        return 1;
    }
    else
    {
        return 0;
    }
}
