class break1 {
    static void print(int i) {
	System.out.println("print: " + i);
    }

    public static void main(String[] args) {
	int total = 0;
	int i = 0;
	while (i < 10) {
	    i = i + 2;
	    if (i == 6) {
		break;
	    }
	    print(i * i);
	    total = total + i;
	}
	print(total);
    }
}
