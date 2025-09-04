package org.wind.tool.third.asm;

final class Frame {

 
    static final int DIM = 0xF0000000;

    static final int ARRAY_OF = 0x10000000;

    static final int ELEMENT_OF = 0xF0000000;

    static final int KIND = 0xF000000;

    static final int TOP_IF_LONG_OR_DOUBLE = 0x800000;

    static final int VALUE = 0x7FFFFF;

    static final int BASE_KIND = 0xFF00000;

    static final int BASE_VALUE = 0xFFFFF;

    static final int BASE = 0x1000000;

    static final int OBJECT = BASE | 0x700000;

    static final int UNINITIALIZED = BASE | 0x800000;

    private static final int LOCAL = 0x2000000;

    private static final int STACK = 0x3000000;

    static final int TOP = BASE | 0;

    static final int BOOLEAN = BASE | 9;

    static final int BYTE = BASE | 10;

    static final int CHAR = BASE | 11;

    static final int SHORT = BASE | 12;

    static final int INTEGER = BASE | 1;

    static final int FLOAT = BASE | 2;

    static final int DOUBLE = BASE | 3;

    static final int LONG = BASE | 4;

    static final int NULL = BASE | 5;

    static final int UNINITIALIZED_THIS = BASE | 6;

    static final int[] SIZE;

    static {
        int i;
        int[] b = new int[202];
        String s = "EFFFFFFFFGGFFFGGFFFEEFGFGFEEEEEEEEEEEEEEEEEEEEDEDEDDDDD"
                + "CDCDEEEEEEEEEEEEEEEEEEEEBABABBBBDCFFFGGGEDCDCDCDCDCDCDCDCD"
                + "CDCEEEEDDDDDDDCDCDCEFEFDDEEFFDEDEEEBDDBBDDDDDDCCCCCCCCEFED"
                + "DDCDCDEEEEEEEEEEFEEEEEEDDEEDDEE";
        for (i = 0; i < b.length; ++i) {
            b[i] = s.charAt(i) - 'E';
        }
        SIZE = b;

    }

    Label owner;

    int[] inputLocals;

    int[] inputStack;

    private int[] outputLocals;

    private int[] outputStack;

    private int outputStackTop;

    private int initializationCount;

    private int[] initializations;

    private int get(final int local) {
        if (outputLocals == null || local >= outputLocals.length) {
            // this local has never been assigned in this basic block,
            // so it is still equal to its value in the input frame
            return LOCAL | local;
        } else {
            int type = outputLocals[local];
            if (type == 0) {
                // this local has never been assigned in this basic block,
                // so it is still equal to its value in the input frame
                type = outputLocals[local] = LOCAL | local;
            }
            return type;
        }
    }

    private void set(final int local, final int type) {
        // creates and/or resizes the output local variables array if necessary
        if (outputLocals == null) {
            outputLocals = new int[10];
        }
        int n = outputLocals.length;
        if (local >= n) {
            int[] t = new int[Math.max(local + 1, 2 * n)];
            System.arraycopy(outputLocals, 0, t, 0, n);
            outputLocals = t;
        }
        // sets the local variable
        outputLocals[local] = type;
    }

    private void push(final int type) {
        // creates and/or resizes the output stack array if necessary
        if (outputStack == null) {
            outputStack = new int[10];
        }
        int n = outputStack.length;
        if (outputStackTop >= n) {
            int[] t = new int[Math.max(outputStackTop + 1, 2 * n)];
            System.arraycopy(outputStack, 0, t, 0, n);
            outputStack = t;
        }
        // pushes the type on the output stack
        outputStack[outputStackTop++] = type;
        // updates the maximun height reached by the output stack, if needed
        int top = owner.inputStackTop + outputStackTop;
        if (top > owner.outputStackMax) {
            owner.outputStackMax = top;
        }
    }

    private void push(final ClassWriter cw, final String desc) {
        int type = type(cw, desc);
        if (type != 0) {
            push(type);
            if (type == LONG || type == DOUBLE) {
                push(TOP);
            }
        }
    }

    private static int type(final ClassWriter cw, final String desc) {
        String t;
        int index = desc.charAt(0) == '(' ? desc.indexOf(')') + 1 : 0;
        switch (desc.charAt(index)) {
            case 'V':
                return 0;
            case 'Z':
            case 'C':
            case 'B':
            case 'S':
            case 'I':
                return INTEGER;
            case 'F':
                return FLOAT;
            case 'J':
                return LONG;
            case 'D':
                return DOUBLE;
            case 'L':
                // stores the internal name, not the descriptor!
                t = desc.substring(index + 1, desc.length() - 1);
                return OBJECT | cw.addType(t);
                // case '[':
            default:
                // extracts the dimensions and the element type
                int data;
                int dims = index + 1;
                while (desc.charAt(dims) == '[') {
                    ++dims;
                }
                switch (desc.charAt(dims)) {
                    case 'Z':
                        data = BOOLEAN;
                        break;
                    case 'C':
                        data = CHAR;
                        break;
                    case 'B':
                        data = BYTE;
                        break;
                    case 'S':
                        data = SHORT;
                        break;
                    case 'I':
                        data = INTEGER;
                        break;
                    case 'F':
                        data = FLOAT;
                        break;
                    case 'J':
                        data = LONG;
                        break;
                    case 'D':
                        data = DOUBLE;
                        break;
                    // case 'L':
                    default:
                        // stores the internal name, not the descriptor
                        t = desc.substring(dims + 1, desc.length() - 1);
                        data = OBJECT | cw.addType(t);
                }
                return (dims - index) << 28 | data;
        }
    }

    private int pop() {
        if (outputStackTop > 0) {
            return outputStack[--outputStackTop];
        } else {
            // if the output frame stack is empty, pops from the input stack
            return STACK | -(--owner.inputStackTop);
        }
    }

    private void pop(final int elements) {
        if (outputStackTop >= elements) {
            outputStackTop -= elements;
        } else {
            // if the number of elements to be popped is greater than the number
            // of elements in the output stack, clear it, and pops the remaining
            // elements from the input stack.
            owner.inputStackTop -= elements - outputStackTop;
            outputStackTop = 0;
        }
    }

    private void pop(final String desc) {
        char c = desc.charAt(0);
        if (c == '(') {
            pop((Type.getArgumentsAndReturnSizes(desc) >> 2) - 1);
        } else if (c == 'J' || c == 'D') {
            pop(2);
        } else {
            pop(1);
        }
    }

    private void init(final int var) {
        // creates and/or resizes the initializations array if necessary
        if (initializations == null) {
            initializations = new int[2];
        }
        int n = initializations.length;
        if (initializationCount >= n) {
            int[] t = new int[Math.max(initializationCount + 1, 2 * n)];
            System.arraycopy(initializations, 0, t, 0, n);
            initializations = t;
        }
        // stores the type to be initialized
        initializations[initializationCount++] = var;
    }

    private int init(final ClassWriter cw, final int t) {
        int s;
        if (t == UNINITIALIZED_THIS) {
            s = OBJECT | cw.addType(cw.thisName);
        } else if ((t & (DIM | BASE_KIND)) == UNINITIALIZED) {
            String type = cw.typeTable[t & BASE_VALUE].strVal1;
            s = OBJECT | cw.addType(type);
        } else {
            return t;
        }
        for (int j = 0; j < initializationCount; ++j) {
            int u = initializations[j];
            int dim = u & DIM;
            int kind = u & KIND;
            if (kind == LOCAL) {
                u = dim + inputLocals[u & VALUE];
            } else if (kind == STACK) {
                u = dim + inputStack[inputStack.length - (u & VALUE)];
            }
            if (t == u) {
                return s;
            }
        }
        return t;
    }

    void initInputFrame(
        final ClassWriter cw,
        final int access,
        final Type[] args,
        final int maxLocals)
    {
        inputLocals = new int[maxLocals];
        inputStack = new int[0];
        int i = 0;
        if ((access & Opcodes.ACC_STATIC) == 0) {
            if ((access & MethodWriter.ACC_CONSTRUCTOR) == 0) {
                inputLocals[i++] = OBJECT | cw.addType(cw.thisName);
            } else {
                inputLocals[i++] = UNINITIALIZED_THIS;
            }
        }
        for (int j = 0; j < args.length; ++j) {
            int t = type(cw, args[j].getDescriptor());
            inputLocals[i++] = t;
            if (t == LONG || t == DOUBLE) {
                inputLocals[i++] = TOP;
            }
        }
        while (i < maxLocals) {
            inputLocals[i++] = TOP;
        }
    }

    void execute(
        final int opcode,
        final int arg,
        final ClassWriter cw,
        final Item item)
    {
        int t1, t2, t3, t4;
        switch (opcode) {
            case Opcodes.NOP:
            case Opcodes.INEG:
            case Opcodes.LNEG:
            case Opcodes.FNEG:
            case Opcodes.DNEG:
            case Opcodes.I2B:
            case Opcodes.I2C:
            case Opcodes.I2S:
            case Opcodes.GOTO:
            case Opcodes.RETURN:
                break;
            case Opcodes.ACONST_NULL:
                push(NULL);
                break;
            case Opcodes.ICONST_M1:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
            case Opcodes.ILOAD:
                push(INTEGER);
                break;
            case Opcodes.LCONST_0:
            case Opcodes.LCONST_1:
            case Opcodes.LLOAD:
                push(LONG);
                push(TOP);
                break;
            case Opcodes.FCONST_0:
            case Opcodes.FCONST_1:
            case Opcodes.FCONST_2:
            case Opcodes.FLOAD:
                push(FLOAT);
                break;
            case Opcodes.DCONST_0:
            case Opcodes.DCONST_1:
            case Opcodes.DLOAD:
                push(DOUBLE);
                push(TOP);
                break;
            case Opcodes.LDC:
                switch (item.type) {
                    case ClassWriter.INT:
                        push(INTEGER);
                        break;
                    case ClassWriter.LONG:
                        push(LONG);
                        push(TOP);
                        break;
                    case ClassWriter.FLOAT:
                        push(FLOAT);
                        break;
                    case ClassWriter.DOUBLE:
                        push(DOUBLE);
                        push(TOP);
                        break;
                    case ClassWriter.CLASS:
                        push(OBJECT | cw.addType("java/lang/Class"));
                        break;
                    // case ClassWriter.STR:
                    default:
                        push(OBJECT | cw.addType("java/lang/String"));
                }
                break;
            case Opcodes.ALOAD:
                push(get(arg));
                break;
            case Opcodes.IALOAD:
            case Opcodes.BALOAD:
            case Opcodes.CALOAD:
            case Opcodes.SALOAD:
                pop(2);
                push(INTEGER);
                break;
            case Opcodes.LALOAD:
            case Opcodes.D2L:
                pop(2);
                push(LONG);
                push(TOP);
                break;
            case Opcodes.FALOAD:
                pop(2);
                push(FLOAT);
                break;
            case Opcodes.DALOAD:
            case Opcodes.L2D:
                pop(2);
                push(DOUBLE);
                push(TOP);
                break;
            case Opcodes.AALOAD:
                pop(1);
                t1 = pop();
                push(ELEMENT_OF + t1);
                break;
            case Opcodes.ISTORE:
            case Opcodes.FSTORE:
            case Opcodes.ASTORE:
                t1 = pop();
                set(arg, t1);
                if (arg > 0) {
                    t2 = get(arg - 1);
                    // if t2 is of kind STACK or LOCAL we cannot know its size!
                    if (t2 == LONG || t2 == DOUBLE) {
                        set(arg - 1, TOP);
                    } else if ((t2 & KIND) != BASE) {
                        set(arg - 1, t2 | TOP_IF_LONG_OR_DOUBLE);
                    }
                }
                break;
            case Opcodes.LSTORE:
            case Opcodes.DSTORE:
                pop(1);
                t1 = pop();
                set(arg, t1);
                set(arg + 1, TOP);
                if (arg > 0) {
                    t2 = get(arg - 1);
                    // if t2 is of kind STACK or LOCAL we cannot know its size!
                    if (t2 == LONG || t2 == DOUBLE) {
                        set(arg - 1, TOP);
                    } else if ((t2 & KIND) != BASE) {
                        set(arg - 1, t2 | TOP_IF_LONG_OR_DOUBLE);
                    }
                }
                break;
            case Opcodes.IASTORE:
            case Opcodes.BASTORE:
            case Opcodes.CASTORE:
            case Opcodes.SASTORE:
            case Opcodes.FASTORE:
            case Opcodes.AASTORE:
                pop(3);
                break;
            case Opcodes.LASTORE:
            case Opcodes.DASTORE:
                pop(4);
                break;
            case Opcodes.POP:
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
            case Opcodes.IFLT:
            case Opcodes.IFGE:
            case Opcodes.IFGT:
            case Opcodes.IFLE:
            case Opcodes.IRETURN:
            case Opcodes.FRETURN:
            case Opcodes.ARETURN:
            case Opcodes.TABLESWITCH:
            case Opcodes.LOOKUPSWITCH:
            case Opcodes.ATHROW:
            case Opcodes.MONITORENTER:
            case Opcodes.MONITOREXIT:
            case Opcodes.IFNULL:
            case Opcodes.IFNONNULL:
                pop(1);
                break;
            case Opcodes.POP2:
            case Opcodes.IF_ICMPEQ:
            case Opcodes.IF_ICMPNE:
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGE:
            case Opcodes.IF_ICMPGT:
            case Opcodes.IF_ICMPLE:
            case Opcodes.IF_ACMPEQ:
            case Opcodes.IF_ACMPNE:
            case Opcodes.LRETURN:
            case Opcodes.DRETURN:
                pop(2);
                break;
            case Opcodes.DUP:
                t1 = pop();
                push(t1);
                push(t1);
                break;
            case Opcodes.DUP_X1:
                t1 = pop();
                t2 = pop();
                push(t1);
                push(t2);
                push(t1);
                break;
            case Opcodes.DUP_X2:
                t1 = pop();
                t2 = pop();
                t3 = pop();
                push(t1);
                push(t3);
                push(t2);
                push(t1);
                break;
            case Opcodes.DUP2:
                t1 = pop();
                t2 = pop();
                push(t2);
                push(t1);
                push(t2);
                push(t1);
                break;
            case Opcodes.DUP2_X1:
                t1 = pop();
                t2 = pop();
                t3 = pop();
                push(t2);
                push(t1);
                push(t3);
                push(t2);
                push(t1);
                break;
            case Opcodes.DUP2_X2:
                t1 = pop();
                t2 = pop();
                t3 = pop();
                t4 = pop();
                push(t2);
                push(t1);
                push(t4);
                push(t3);
                push(t2);
                push(t1);
                break;
            case Opcodes.SWAP:
                t1 = pop();
                t2 = pop();
                push(t1);
                push(t2);
                break;
            case Opcodes.IADD:
            case Opcodes.ISUB:
            case Opcodes.IMUL:
            case Opcodes.IDIV:
            case Opcodes.IREM:
            case Opcodes.IAND:
            case Opcodes.IOR:
            case Opcodes.IXOR:
            case Opcodes.ISHL:
            case Opcodes.ISHR:
            case Opcodes.IUSHR:
            case Opcodes.L2I:
            case Opcodes.D2I:
            case Opcodes.FCMPL:
            case Opcodes.FCMPG:
                pop(2);
                push(INTEGER);
                break;
            case Opcodes.LADD:
            case Opcodes.LSUB:
            case Opcodes.LMUL:
            case Opcodes.LDIV:
            case Opcodes.LREM:
            case Opcodes.LAND:
            case Opcodes.LOR:
            case Opcodes.LXOR:
                pop(4);
                push(LONG);
                push(TOP);
                break;
            case Opcodes.FADD:
            case Opcodes.FSUB:
            case Opcodes.FMUL:
            case Opcodes.FDIV:
            case Opcodes.FREM:
            case Opcodes.L2F:
            case Opcodes.D2F:
                pop(2);
                push(FLOAT);
                break;
            case Opcodes.DADD:
            case Opcodes.DSUB:
            case Opcodes.DMUL:
            case Opcodes.DDIV:
            case Opcodes.DREM:
                pop(4);
                push(DOUBLE);
                push(TOP);
                break;
            case Opcodes.LSHL:
            case Opcodes.LSHR:
            case Opcodes.LUSHR:
                pop(3);
                push(LONG);
                push(TOP);
                break;
            case Opcodes.IINC:
                set(arg, INTEGER);
                break;
            case Opcodes.I2L:
            case Opcodes.F2L:
                pop(1);
                push(LONG);
                push(TOP);
                break;
            case Opcodes.I2F:
                pop(1);
                push(FLOAT);
                break;
            case Opcodes.I2D:
            case Opcodes.F2D:
                pop(1);
                push(DOUBLE);
                push(TOP);
                break;
            case Opcodes.F2I:
            case Opcodes.ARRAYLENGTH:
            case Opcodes.INSTANCEOF:
                pop(1);
                push(INTEGER);
                break;
            case Opcodes.LCMP:
            case Opcodes.DCMPL:
            case Opcodes.DCMPG:
                pop(4);
                push(INTEGER);
                break;
            case Opcodes.JSR:
            case Opcodes.RET:
                throw new RuntimeException("JSR/RET are not supported with computeFrames option");
            case Opcodes.GETSTATIC:
                push(cw, item.strVal3);
                break;
            case Opcodes.PUTSTATIC:
                pop(item.strVal3);
                break;
            case Opcodes.GETFIELD:
                pop(1);
                push(cw, item.strVal3);
                break;
            case Opcodes.PUTFIELD:
                pop(item.strVal3);
                pop();
                break;
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEINTERFACE:
                pop(item.strVal3);
                if (opcode != Opcodes.INVOKESTATIC) {
                    t1 = pop();
                    if (opcode == Opcodes.INVOKESPECIAL
                            && item.strVal2.charAt(0) == '<')
                    {
                        init(t1);
                    }
                }
                push(cw, item.strVal3);
                break;
            case Opcodes.INVOKEDYNAMIC:
                pop(item.strVal2);
                push(cw, item.strVal2);
                break;
            case Opcodes.NEW:
                push(UNINITIALIZED | cw.addUninitializedType(item.strVal1, arg));
                break;
            case Opcodes.NEWARRAY:
                pop();
                switch (arg) {
                    case Opcodes.T_BOOLEAN:
                        push(ARRAY_OF | BOOLEAN);
                        break;
                    case Opcodes.T_CHAR:
                        push(ARRAY_OF | CHAR);
                        break;
                    case Opcodes.T_BYTE:
                        push(ARRAY_OF | BYTE);
                        break;
                    case Opcodes.T_SHORT:
                        push(ARRAY_OF | SHORT);
                        break;
                    case Opcodes.T_INT:
                        push(ARRAY_OF | INTEGER);
                        break;
                    case Opcodes.T_FLOAT:
                        push(ARRAY_OF | FLOAT);
                        break;
                    case Opcodes.T_DOUBLE:
                        push(ARRAY_OF | DOUBLE);
                        break;
                    // case Opcodes.T_LONG:
                    default:
                        push(ARRAY_OF | LONG);
                        break;
                }
                break;
            case Opcodes.ANEWARRAY:
                String s = item.strVal1;
                pop();
                if (s.charAt(0) == '[') {
                    push(cw, '[' + s);
                } else {
                    push(ARRAY_OF | OBJECT | cw.addType(s));
                }
                break;
            case Opcodes.CHECKCAST:
                s = item.strVal1;
                pop();
                if (s.charAt(0) == '[') {
                    push(cw, s);
                } else {
                    push(OBJECT | cw.addType(s));
                }
                break;
            // case Opcodes.MULTIANEWARRAY:
            default:
                pop(arg);
                push(cw, item.strVal1);
                break;
        }
    }

    boolean merge(final ClassWriter cw, final Frame frame, final int edge) {
        boolean changed = false;
        int i, s, dim, kind, t;

        int nLocal = inputLocals.length;
        int nStack = inputStack.length;
        if (frame.inputLocals == null) {
            frame.inputLocals = new int[nLocal];
            changed = true;
        }

        for (i = 0; i < nLocal; ++i) {
            if (outputLocals != null && i < outputLocals.length) {
                s = outputLocals[i];
                if (s == 0) {
                    t = inputLocals[i];
                } else {
                    dim = s & DIM;
                    kind = s & KIND;
                    if (kind == BASE) {
                        t = s;
                    } else {
                        if (kind == LOCAL) {
                            t = dim + inputLocals[s & VALUE];
                        } else {
                            t = dim + inputStack[nStack - (s & VALUE)];
                        }
                        if ((s & TOP_IF_LONG_OR_DOUBLE) != 0 && (t == LONG || t == DOUBLE)) {
                            t = TOP;
                        }
                    }
                }
            } else {
                t = inputLocals[i];
            }
            if (initializations != null) {
                t = init(cw, t);
            }
            changed |= merge(cw, t, frame.inputLocals, i);
        }

        if (edge > 0) {
            for (i = 0; i < nLocal; ++i) {
                t = inputLocals[i];
                changed |= merge(cw, t, frame.inputLocals, i);
            }
            if (frame.inputStack == null) {
                frame.inputStack = new int[1];
                changed = true;
            }
            changed |= merge(cw, edge, frame.inputStack, 0);
            return changed;
        }

        int nInputStack = inputStack.length + owner.inputStackTop;
        if (frame.inputStack == null) {
            frame.inputStack = new int[nInputStack + outputStackTop];
            changed = true;
        }

        for (i = 0; i < nInputStack; ++i) {
            t = inputStack[i];
            if (initializations != null) {
                t = init(cw, t);
            }
            changed |= merge(cw, t, frame.inputStack, i);
        }
        for (i = 0; i < outputStackTop; ++i) {
            s = outputStack[i];
            dim = s & DIM;
            kind = s & KIND;
            if (kind == BASE) {
                t = s;
            } else {
                if (kind == LOCAL) {
                    t = dim + inputLocals[s & VALUE];
                } else {
                    t = dim + inputStack[nStack - (s & VALUE)];
                }
                if ((s & TOP_IF_LONG_OR_DOUBLE) != 0 && (t == LONG || t == DOUBLE)) {
                    t = TOP;
                }
            }
            if (initializations != null) {
                t = init(cw, t);
            }
            changed |= merge(cw, t, frame.inputStack, nInputStack + i);
        }
        return changed;
    }

    private static boolean merge(
        final ClassWriter cw,
        int t,
        final int[] types,
        final int index)
    {
        int u = types[index];
        if (u == t) {
            // if the types are equal, merge(u,t)=u, so there is no change
            return false;
        }
        if ((t & ~DIM) == NULL) {
            if (u == NULL) {
                return false;
            }
            t = NULL;
        }
        if (u == 0) {
            // if types[index] has never been assigned, merge(u,t)=t
            types[index] = t;
            return true;
        }
        int v;
        if ((u & BASE_KIND) == OBJECT || (u & DIM) != 0) {
            // if u is a reference type of any dimension
            if (t == NULL) {
                // if t is the NULL type, merge(u,t)=u, so there is no change
                return false;
            } else if ((t & (DIM | BASE_KIND)) == (u & (DIM | BASE_KIND))) {
                if ((u & BASE_KIND) == OBJECT) {
                    // if t is also a reference type, and if u and t have the
                    // same dimension merge(u,t) = dim(t) | common parent of the
                    // element types of u and t
                    v = (t & DIM) | OBJECT
                            | cw.getMergedType(t & BASE_VALUE, u & BASE_VALUE);
                } else {
                    // if u and t are array types, but not with the same element
                    // type, merge(u,t)=java/lang/Object
                    v = OBJECT | cw.addType("java/lang/Object");
                }
            } else if ((t & BASE_KIND) == OBJECT || (t & DIM) != 0) {
                // if t is any other reference or array type,
                // merge(u,t)=java/lang/Object
                v = OBJECT | cw.addType("java/lang/Object");
            } else {
                // if t is any other type, merge(u,t)=TOP
                v = TOP;
            }
        } else if (u == NULL) {
            // if u is the NULL type, merge(u,t)=t,
            // or TOP if t is not a reference type
            v = (t & BASE_KIND) == OBJECT || (t & DIM) != 0 ? t : TOP;
        } else {
            // if u is any other type, merge(u,t)=TOP whatever t
            v = TOP;
        }
        if (u != v) {
            types[index] = v;
            return true;
        }
        return false;
    }
}
