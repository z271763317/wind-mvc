package org.wind.tool.third.asm;

public class Label {

    static final int DEBUG = 1;

    static final int RESOLVED = 2;

    static final int RESIZED = 4;

    static final int PUSHED = 8;

    static final int TARGET = 16;

    static final int STORE = 32;

    static final int REACHABLE = 64;

    static final int JSR = 128;

    static final int RET = 256;

    static final int SUBROUTINE = 512;

    static final int VISITED = 1024;

    static final int VISITED2 = 2048;

    public Object info;

    int status;

    int line;

    int position;

    private int referenceCount;

    private int[] srcAndRefPositions;

    // ------------------------------------------------------------------------

    int inputStackTop;

    int outputStackMax;

    Frame frame;

    Label successor;

    Edge successors;

    Label next;

    public Label() {
    }

    public int getOffset() {
        if ((status & RESOLVED) == 0) {
            throw new IllegalStateException("Label offset position has not been resolved yet");
        }
        return position;
    }

    void put(
        final MethodWriter owner,
        final ByteVector out,
        final int source,
        final boolean wideOffset)
    {
        if ((status & RESOLVED) == 0) {
            if (wideOffset) {
                addReference(-1 - source, out.length);
                out.putInt(-1);
            } else {
                addReference(source, out.length);
                out.putShort(-1);
            }
        } else {
            if (wideOffset) {
                out.putInt(position - source);
            } else {
                out.putShort(position - source);
            }
        }
    }

    private void addReference(
        final int sourcePosition,
        final int referencePosition)
    {
        if (srcAndRefPositions == null) {
            srcAndRefPositions = new int[6];
        }
        if (referenceCount >= srcAndRefPositions.length) {
            int[] a = new int[srcAndRefPositions.length + 6];
            System.arraycopy(srcAndRefPositions,
                    0,
                    a,
                    0,
                    srcAndRefPositions.length);
            srcAndRefPositions = a;
        }
        srcAndRefPositions[referenceCount++] = sourcePosition;
        srcAndRefPositions[referenceCount++] = referencePosition;
    }

    boolean resolve(
        final MethodWriter owner,
        final int position,
        final byte[] data)
    {
        boolean needUpdate = false;
        this.status |= RESOLVED;
        this.position = position;
        int i = 0;
        while (i < referenceCount) {
            int source = srcAndRefPositions[i++];
            int reference = srcAndRefPositions[i++];
            int offset;
            if (source >= 0) {
                offset = position - source;
                if (offset < Short.MIN_VALUE || offset > Short.MAX_VALUE) {
                   
                    int opcode = data[reference - 1] & 0xFF;
                    if (opcode <= Opcodes.JSR) {
                        // changes IFEQ ... JSR to opcodes 202 to 217
                        data[reference - 1] = (byte) (opcode + 49);
                    } else {
                        // changes IFNULL and IFNONNULL to opcodes 218 and 219
                        data[reference - 1] = (byte) (opcode + 20);
                    }
                    needUpdate = true;
                }
                data[reference++] = (byte) (offset >>> 8);
                data[reference] = (byte) offset;
            } else {
                offset = position + source + 1;
                data[reference++] = (byte) (offset >>> 24);
                data[reference++] = (byte) (offset >>> 16);
                data[reference++] = (byte) (offset >>> 8);
                data[reference] = (byte) offset;
            }
        }
        return needUpdate;
    }

    Label getFirst() {
        return !ClassReader.FRAMES || frame == null ? this : frame.owner;
    }

    boolean inSubroutine(final long id) {
        if ((status & Label.VISITED) != 0) {
            return (srcAndRefPositions[(int) (id >>> 32)] & (int) id) != 0;
        }
        return false;
    }

    boolean inSameSubroutine(final Label block) {
        if ((status & VISITED) == 0 || (block.status & VISITED) == 0) {
            return false;
        }
        for (int i = 0; i < srcAndRefPositions.length; ++i) {
            if ((srcAndRefPositions[i] & block.srcAndRefPositions[i]) != 0) {
                return true;
            }
        }
        return false;
    }

    void addToSubroutine(final long id, final int nbSubroutines) {
        if ((status & VISITED) == 0) {
            status |= VISITED;
            srcAndRefPositions = new int[(nbSubroutines - 1) / 32 + 1];
        }
        srcAndRefPositions[(int) (id >>> 32)] |= (int) id;
    }
    
    void visitSubroutine(final Label JSR, final long id, final int nbSubroutines)
    {
        // user managed stack of labels, to avoid using a recursive method
        // (recursivity can lead to stack overflow with very large methods)
        Label stack = this;
        while (stack != null) {
            // removes a label l from the stack
            Label l = stack;
            stack = l.next;
            l.next = null;
            
            if (JSR != null) {
                if ((l.status & VISITED2) != 0) {
                    continue;
                }
                l.status |= VISITED2;
                // adds JSR to the successors of l, if it is a RET block
                if ((l.status & RET) != 0) {
                    if (!l.inSameSubroutine(JSR)) {
                        Edge e = new Edge();
                        e.info = l.inputStackTop;
                        e.successor = JSR.successors.successor;
                        e.next = l.successors;
                        l.successors = e;
                    }
                }
            } else {
                // if the l block already belongs to subroutine 'id', continue
                if (l.inSubroutine(id)) {
                    continue;
                }
                // marks the l block as belonging to subroutine 'id'
                l.addToSubroutine(id, nbSubroutines);            
            }
            // pushes each successor of l on the stack, except JSR targets
            Edge e = l.successors;
            while (e != null) {
                // if the l block is a JSR block, then 'l.successors.next' leads
                // to the JSR target (see {@link #visitJumpInsn}) and must 
                // therefore not be followed
                if ((l.status & Label.JSR) == 0 || e != l.successors.next) {
                    // pushes e.successor on the stack if it not already added
                    if (e.successor.next == null) {
                        e.successor.next = stack;
                        stack = e.successor;
                    }
                }
                e = e.next;
            }
        }
    }

    public String toString() {
        return "L" + System.identityHashCode(this);
    }
}
