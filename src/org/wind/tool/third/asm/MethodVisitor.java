package org.wind.tool.third.asm;

public interface MethodVisitor {

    // -------------------------------------------------------------------------
    // Annotations and non standard attributes
    // -------------------------------------------------------------------------

    AnnotationVisitor visitAnnotationDefault();

    AnnotationVisitor visitAnnotation(String desc, boolean visible);

    AnnotationVisitor visitParameterAnnotation(
        int parameter,
        String desc,
        boolean visible);

    void visitAttribute(Attribute attr);

    void visitCode();

    void visitFrame(
        int type,
        int nLocal,
        Object[] local,
        int nStack,
        Object[] stack);

    // -------------------------------------------------------------------------
    // Normal instructions
    // -------------------------------------------------------------------------

    void visitInsn(int opcode);

    void visitIntInsn(int opcode, int operand);

    void visitVarInsn(int opcode, int var);

    void visitTypeInsn(int opcode, String type);

    void visitFieldInsn(int opcode, String owner, String name, String desc);

    void visitMethodInsn(int opcode, String owner, String name, String desc);

    void visitJumpInsn(int opcode, Label label);

    void visitLabel(Label label);

    // -------------------------------------------------------------------------
    // Special instructions
    // -------------------------------------------------------------------------

    void visitLdcInsn(Object cst);

    void visitIincInsn(int var, int increment);

    void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels);

    void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels);

    void visitMultiANewArrayInsn(String desc, int dims);

    // -------------------------------------------------------------------------
    // Exceptions table entries, debug information, max stack and max locals
    // -------------------------------------------------------------------------

    void visitTryCatchBlock(Label start, Label end, Label handler, String type);

    void visitLocalVariable(
        String name,
        String desc,
        String signature,
        Label start,
        Label end,
        int index);

    void visitLineNumber(int line, Label start);

    void visitMaxs(int maxStack, int maxLocals);

    void visitEnd();
}
