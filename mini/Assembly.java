package mini;

import compiler.*;
import compiler.Failure;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/** Provides a simple mechanism for assembly language output.
 */
public class Assembly {

    /** In the current system, we assume that all values can be represented
     *  by a single word whose size in bytes is given by the WORDSIZE constant.
     */
    public static final int WORDSIZE = 4;

    /** Specifies the name of the entry point to the compiled code.
     */
    static final String entry = "mini_main";

    /** Set the platform flag for this machine.
     */
    public final int platform = MACOSX;

    /** Include this code in the platform choice if external symbols
     *  require a leading underscore.
     */
    static final int UNDERSCORES = 1;

    /** Include this code in the platform choice if the code generator
     *  should avoid using the ebx register.
     */
    static final int AVOID_EBX = 2;

    /** Include this code in the platform choice if the code generator
     *  should ensure that stack frames are aligned on a 16 byte boundary.
     */
    static final int ALIGN16 = 4;

    /** Platform flags for Linux.
     */
    static final int LINUX = AVOID_EBX;

    /** Platform flags for Mac OS X.
     */
    static final int MACOSX = UNDERSCORES | ALIGN16;

    /** Platform flags for Windows.
     */
    static final int WINDOWS = UNDERSCORES;

    /** Used to store the output stream for this assembly file.
     */
    private PrintWriter out;

    /** A private constructor, used from the assembleToFile() method.
     */
    private Assembly(PrintWriter out) {
        this.out = out;
    }

    /** A convenience method that creates an assembly object for
     *  a named output file.
     */
    public static Assembly assembleToFile(String name) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(name));
            Assembly a = new Assembly(out);
            a.emit(".file",  "\"" + name + "\"");
            a.emit(".globl", a.name(Assembly.entry));
            return a;
        } catch (IOException e) {
            return null;
        }
    }

    /** Close this Assembly object and free up associated resources.
     */
    public void close() {
        out.close();
        out = null;
    }

    /** A counter that is used to generate new labels; the counter is
     *  incremented each time a new label is produced.
     */
    private int labelCounter = 0;

    /** Generate a string for a label from an integer input.
     *  We require that distinct inputs produce distinct outputs
     *  and that none of the generated label names can clash with
     *  names in user programs.
     */
    public String label(int l) {
        return "l" + l;
    }

    /** Return a fresh (i.e., previously unused) label name.
     */
    public String newLabel() {
        return label(labelCounter++);
    }

    /** Output a label at the beginning of a line.
     */
    public void emitLabel(String name) {
        handlePendingAdjust();
        out.print(name);
        out.println(":");
    }

    /** Output a function/variable name using the appropriate
     *  platform naming conventions with respect to underscores.
     */
    public String name(String n) {
        return (platform & UNDERSCORES)==0 ? n : ("_" + n);
    }
    private String[] regs = (platform & AVOID_EBX)!=0
        ? new String[] { "%eax",         "%ecx", "%esi", "%edi" }
        : new String[] { "%eax", "%ebx", "%ecx", "%esi", "%edi" };
    private int numRegs = regs.length;

    /** Return the name of the physical register corresponding to a
     *  specific logical register.
     */
    public String reg(int free) {
        return regs[free % numRegs];
    }

    /** Determine whether spilling is required to access the specified
     *  logical register (i.e., whether the same physical register is
     *  already being used to hold the value of a different, lower index
     *  logical register.
     */
    public boolean spillFor(int free) {
        return (free>=numRegs);
    }

    /** Output code to preserve the value in a register that is
     *  currently in use by pushing its value to the stack.  This
     *  allows that same register to be used temporarily to hold
     *  the value for a different logical register.  Returns the
     *  number of bytes pushed on to the stack in the process.
     */
    public int spill(int free) {
        if (spillFor(free)) {
            emit("pushl", reg(free));
            return WORDSIZE;
        } else {
            return 0;
        }
    }

    /** Output code to restore a register to its previously
     *  held value, if any, by popping the old value off of
     *  the stack.
     */
    public void unspill(int free) {
        if (spillFor(free)) {
            emit("popl", reg(free));
        }
    }

    /** Spill all registers currently holding temporary values
     *  to the stack in preparation for a function call.  Returns
     *  the number of bytes pushed on to the stack in the process.
     */
    public int spillAll(int free) {
        int toSpill = Math.min(free, numRegs-1);
        int bytes   = 0;
        for (int i=1; i<=toSpill; i++) {
            emit("pushl", reg(free-i));
            bytes += Assembly.WORDSIZE;
        }
        return bytes;
    }

    /** Restore previously spilled registers from the stack
     *  after a function call.
     */
    public void unspillAll(int free) {
        int toSpill = Math.min(free, numRegs-1);
        for (int i=toSpill; i>=1; i--) {
            emit("popl", reg(free-i));
        }
    }

    /** Emit instructions for a simple function call.  On platforms with
     *  ALIGN16, the stack pointer should be 16 byte aligned at this point.
     */
    public void call(String lab, int free) {
        emit("call", name(lab));
        if ((free%numRegs)!=0) {
            emit("movl", reg(0), reg(free));
        }
    }

    /** Emit an instruction with no operands.
     */
    public void emit(String op) {
        handlePendingAdjust();
        out.println("\t" + op);
    }

    /** Emit an instruction with one operand.
     */
    public void emit(String op, String op1) {
        handlePendingAdjust();
        out.println("\t" + op + "\t" + op1);
    }

    /** Emit an instruction with two operands.
     */
    public void emit(String op, String op1, String op2) {
        handlePendingAdjust();
        out.println("\t" + op + "\t" + op1 + "," + op2);
    }

    /** Return a number as a string for use in contexts where only
     *  a number is allowed and hence no special characters are
     *  required to distinguish an addressing mode.
     */
    public String number(int v) {
        return Integer.toString(v);
    }

    /** Return a string for an operand using immediate addressing.
     */
    public String immed(int v) {
        return "$" + v;
    }

    /** Return a reference to a memory location using indirect
     *  addressing.
     */
    public String indirect(int n, String s) {
        if (n==0) {
            return "(" + s + ")";
        } else {
            return n + "(" + s + ")";
        }
    }

    /** Track the number of bytes by which the stack pointer should be
     *  adjusted before the next instruction.  This mechanism allows us
     *  to combine multiple adjustments into a single instruction, and
     *  to omit adjustments completely when they are not required (for
     *  example, immediately before a function's epilogue).
     */
    private int pendingAdjust = 0;

    /** Check to see if a stack adjustment is required before the next
     *  instruction is emitted.
     */
    public void handlePendingAdjust() {
        if (pendingAdjust!=0) {
            int adjust    = pendingAdjust;
            pendingAdjust = 0;
            if (adjust>0) {
                emit("subl", immed(adjust),  "%esp");
            } else {
                emit("addl", immed(-adjust), "%esp");
            }
        }
    }

    /** Adjust the stack by inserting space for the specified number of
     *  bytes.  Can be used to reserve space for locals, or to establish
     *  alignment constraints.
     */
    void insertAdjust(int adjust) {
        pendingAdjust += adjust;
    }

    /** Adjust the stack by removing space for the specified number of
     *  bytes.
     */
    void removeAdjust(int adjust) {
        pendingAdjust -= adjust;
    }

    /** Calculate how many additional bytes need to be pushed onto
     *  the stack to ensure correct alignment once "pushed" bytes
     *  have been pushed, and allowing for an extra word to be pushed
     *  for a return address.  
     */
    int alignmentAdjust(int pushed) {
        // For platforms that need it (i.e., Mac OS X), we determine
        // how many extra bytes must be added to the stack to ensure
        // alignment on a 16 byte boundary.  For other platforms, we
        // can just return zero.
        return ((platform & ALIGN16)==0)
                 ? 0
                 : ((16 - (pushed + WORDSIZE)) & 15);
    }

    /** Output the prologue code section at the start of a program.
     *  Returns the number of bytes that have been pushed onto the
     *  stack since the function was entered.
     */
    public int emitPrologue(int localBytes) {
        emit("pushl", "%ebp");
        emit("movl",  "%esp", "%ebp");
        pendingAdjust = localBytes;
        return localBytes + WORDSIZE;
    }

    /** return a string that can be used to reference the memory
     *  location that is being used for a specific variable.
     */
    public String varRef(VarEnv ve) {
        return indirect(ve.getOffset(), "%ebp");
    }

    /** Output the epilogue code section at the end of a program.
     */
    public void emitEpilogue() {
        pendingAdjust = 0;
        emit("movl", "%ebp", "%esp");
        emit("popl", "%ebp");
        emit("ret");
    }
}
