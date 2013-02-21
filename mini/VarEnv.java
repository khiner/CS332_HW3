package mini;

import compiler.*;
import compiler.Failure;

/** Represents a variable environment, mapping identifiers
 *  to corresponding types.
 */
public class VarEnv {
    private Id      id;
    private Type    type;
    private int     offset;
    private boolean byRef;
    private VarEnv  next;

    public VarEnv(Id id, Type type, int offset, boolean byRef, VarEnv next) {
        this.id     = id;
        this.type   = type;
        this.offset = offset;
        this.byRef  = byRef;
        this.next   = next;
    }

    /** Return the type associated with this variable environment.
     */
    public Type getType() {
        return type;
    }

    /** Return the stack frame offset that will be used for this variable.
     */
    public int getOffset() {
        return offset;
    }

    /** Return a flag to indicate if this variable is a by reference parameter.
     */
    public boolean isByRef() {
        return byRef;
    }

    /** Search an environment for a specified variable name, returning
     *  null if no such entry is found, or else returning a pointer to
     *  the first matching VarEnv object in the list.
     */
    public static VarEnv find(Id id, VarEnv env) {
        while (env!=null && !env.id.equals(id)) {
            env = env.next;
        }
        return env;
    }
}
