int x = 0;
int n = 20;
int check_x = -1;
void *t1() {
    {
        check_x = 1
    }
}

void *t2() {
    int t;
    t = x;
    x = t + 1;
}

int nondet_loop_bound_2()
{
	t1();

	t2();
	t2();
	t2();
	t2();
	t2();

	t2();
	t2();
	t2();
	t2();
	t2();

	t2();
	t2();
	t2();
	t2();
	t2();

	t2();
	t2();
	t2();
	t2();
	t2();

	if (check_x == 1)
	{
	    return 1;
	}
	else
	{
	    return 0;
	}
}

