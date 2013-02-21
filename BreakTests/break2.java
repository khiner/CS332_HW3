class break2 {
    static void print(int i) {
	System.out.println("print: " + i);
    }

    public static void main(String[] args) {
	int total = 0;
	int i = 0;
	for (;i < 10;i = i + 1) {
	    if (i == 6) {
		break;
	    }
	    print(i * i);
	    total = total + i;
	}
	print(total);
    }
}
