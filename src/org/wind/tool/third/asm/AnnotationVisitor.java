package org.wind.tool.third.asm;

public interface AnnotationVisitor {

    void visit(String name, Object value);

    void visitEnum(String name, String desc, String value);

    AnnotationVisitor visitAnnotation(String name, String desc);

    AnnotationVisitor visitArray(String name);

    void visitEnd();
}
