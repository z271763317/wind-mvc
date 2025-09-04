package org.wind.tool.third.asm;

public interface FieldVisitor {

    AnnotationVisitor visitAnnotation(String desc, boolean visible);

    void visitAttribute(Attribute attr);

    void visitEnd();
}
