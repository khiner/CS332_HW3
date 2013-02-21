package mini;

import compiler.Failure;

/** Abstract syntax for "for" loops.
 */
class For extends LoopStmt {
    private ForInit init;
    private Expr    step;

    private String stepLabel = null;

    For(ForInit init, Expr test, Expr step, Stmt body) {
        this.init = init;
        this.test = test;
        this.step = step;
        this.body = body;
    }

    /** Check that this statement is valid, taking the current environment
     *  as an argument and returning a possibly modified environment as a
     *  result.
     */
    public VarEnv check(Fundef def, Context ctxt, LoopStmt parentLoop, VarEnv env)
      throws Failure {

        // Find the environment that is produced by the initializer
        VarEnv ienv = init.check(def, ctxt, env);

        // Check that the test is a Boolean
        try {
            if (test!=null && !test.typeOf(ctxt, ienv).equal(Type.BOOLEAN)) {
               ctxt.report(new Failure("Boolean test expected in for loop"));
            }
        } catch (Failure f) {
            // report any error that occured while checking the expression.
            ctxt.report(f);
        }

        // Check that the step is a valid expression
        if (step!=null) {
            step.typeOf(ctxt, ienv);
        }

        // check body, but discard any modified environment that it produces.
        body.check(def, ctxt, this, ienv);

        def.returns = false;
        return env;
    }

    /*
     * For loops continue to a 'step' expression before going to the test
     */
    public String getContinueLabel() {
	return stepLabel;
    }

    /** For loops have an additional 'step' expression that needs a label
     */
    @Override
    public void initLabels(Assembly a) {
	super.initLabels(a);
	stepLabel = a.newLabel();
    }

    /** test / end label emission and test branching are handled in LoopStmt parent class
     *  All else handled here.
     */
    protected void compileInner(Assembly a, int pushed) {
	init.compile(a, pushed);
	a.emit("jmp", testLabel);
	a.emitLabel(bodyLabel);
	body.compile(a, pushed);
	a.emitLabel(stepLabel);
	if (step != null) {
	    step.compileExpr(a, pushed, 0);
	}
    }
}
