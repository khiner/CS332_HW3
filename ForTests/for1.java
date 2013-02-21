class for1 {
    static void print(int i) {
	System.out.println("print: " + i);
    }

    public static void main(String[] args) {
	int total = 0;
	for (int i = 0; i < 10; i = i + 1) {
	    print(i * i);
	    total = total + i;
	}
	print(total);
    }
}
