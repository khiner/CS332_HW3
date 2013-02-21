package mini;

public abstract class LoopStmt extends Stmt {
    protected Expr    test;
    protected Stmt    body;

    protected String testLabel = null;
    protected String bodyLabel = null;
    protected String endLabel = null;

    /** loop statements extending this class must emit / jump to body label,
     *  as well as any other details specific to the implementation
     */
    protected abstract void compileInner(Assembly a, int pushed);

    /*
     * Returns the label that should be jumped to on a 'continue' statement.
     * This label will be different for different loops.
     */
    public abstract String getContinueLabel();

    /*
     * Returns the label leading right into the body of this loop
     */
    public String getBodyLabel() {
	return bodyLabel;
    }

    /*
     * Returns the label directly after this label.  Used for the 'break' statement
     */
    public String getEndLabel() {
	return endLabel;
    }

    /** test / end label emission and test branching are handled.
     */
    public void compile(Assembly a, int pushed) {
	initLabels(a);
	compileInner(a, pushed);
	a.emitLabel(testLabel);
	test.branchTrue(a, pushed, 0, bodyLabel);
	a.emitLabel(endLabel);
    }

    protected void initLabels(Assembly a) {
	testLabel = a.newLabel();
	bodyLabel = a.newLabel();
	endLabel = a.newLabel();
    }
}
