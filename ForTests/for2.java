class for2 {
    static void print(int i) {
	System.out.println("print: " + i);
    }

    public static void main(String[] args) {
	int total = 0;
	int i = 5;
	for (; i < 10;) {
	    print(i * i);
	    total = total + i;
	    i = i + 1;
	}
	print(total);
    }
}
