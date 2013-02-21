package mini;

import compiler.*;
import compiler.Failure;

/** Abstract syntax for continue statements.
 */
class Continue extends Stmt {

    LoopStmt parentLoop = null;

    /** Check that this statement is valid, taking the current environment
     *  as an argument and returning a possibly modified environment as a
     *  result.
     */
    public VarEnv check(Fundef def, Context ctxt, LoopStmt parentLoop, VarEnv env)
      throws Failure {
        if (parentLoop == null) {
            ctxt.report(new Failure("continue can only be used in a loop"));
        }
	this.parentLoop = parentLoop;
        def.returns = false;
        return env; // Return the input environment unchanged.
    }

    /** Generate code for executing this statement.
     */
    public void compile(Assembly a, int pushed) {
	if (parentLoop != null) {
	    // unconditionally jump to the parent-loop's test (if we are in a loop)
	    a.emit("jmp", parentLoop.getContinueLabel());
	}
    }
}
