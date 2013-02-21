class for3 {
    static void print(int i) {
	System.out.println("print: " + i);
    }

    public static void main(String[] args) {
	int total = 0;
	int i = 0;
	for (; i < 10;) {
	    print(i * i);
	    total = total + i;
	    i = i + 2;
	}
	print(total);
    }
}
