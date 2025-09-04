package org.wind.tool.third.asm;

final class FieldWriter implements FieldVisitor {

    FieldWriter next;

    private final ClassWriter cw;

    private final int access;

    private final int name;

    private final int desc;

    private int signature;

    private int value;

    private AnnotationWriter anns;

    private AnnotationWriter ianns;

    private Attribute attrs;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    FieldWriter(
        final ClassWriter cw,
        final int access,
        final String name,
        final String desc,
        final String signature,
        final Object value)
    {
        if (cw.firstField == null) {
            cw.firstField = this;
        } else {
            cw.lastField.next = this;
        }
        cw.lastField = this;
        this.cw = cw;
        this.access = access;
        this.name = cw.newUTF8(name);
        this.desc = cw.newUTF8(desc);
        if (ClassReader.SIGNATURES && signature != null) {
            this.signature = cw.newUTF8(signature);
        }
        if (value != null) {
            this.value = cw.newConstItem(value).index;
        }
    }

    // ------------------------------------------------------------------------
    // Implementation of the FieldVisitor interface
    // ------------------------------------------------------------------------

    public AnnotationVisitor visitAnnotation(
        final String desc,
        final boolean visible)
    {
        if (!ClassReader.ANNOTATIONS) {
            return null;
        }
        ByteVector bv = new ByteVector();
        // write type, and reserve space for values count
        bv.putShort(cw.newUTF8(desc)).putShort(0);
        AnnotationWriter aw = new AnnotationWriter(cw, true, bv, bv, 2);
        if (visible) {
            aw.next = anns;
            anns = aw;
        } else {
            aw.next = ianns;
            ianns = aw;
        }
        return aw;
    }

    public void visitAttribute(final Attribute attr) {
        attr.next = attrs;
        attrs = attr;
    }

    public void visitEnd() {
    }

    // ------------------------------------------------------------------------
    // Utility methods
    // ------------------------------------------------------------------------

    int getSize() {
        int size = 8;
        if (value != 0) {
            cw.newUTF8("ConstantValue");
            size += 8;
        }
        if ((access & Opcodes.ACC_SYNTHETIC) != 0
                && ((cw.version & 0xFFFF) < Opcodes.V1_5 || (access & ClassWriter.ACC_SYNTHETIC_ATTRIBUTE) != 0))
        {
            cw.newUTF8("Synthetic");
            size += 6;
        }
        if ((access & Opcodes.ACC_DEPRECATED) != 0) {
            cw.newUTF8("Deprecated");
            size += 6;
        }
        if (ClassReader.SIGNATURES && signature != 0) {
            cw.newUTF8("Signature");
            size += 8;
        }
        if (ClassReader.ANNOTATIONS && anns != null) {
            cw.newUTF8("RuntimeVisibleAnnotations");
            size += 8 + anns.getSize();
        }
        if (ClassReader.ANNOTATIONS && ianns != null) {
            cw.newUTF8("RuntimeInvisibleAnnotations");
            size += 8 + ianns.getSize();
        }
        if (attrs != null) {
            size += attrs.getSize(cw, null, 0, -1, -1);
        }
        return size;
    }

    void put(final ByteVector out) {
        int mask = Opcodes.ACC_DEPRECATED
                | ClassWriter.ACC_SYNTHETIC_ATTRIBUTE
                | ((access & ClassWriter.ACC_SYNTHETIC_ATTRIBUTE) / (ClassWriter.ACC_SYNTHETIC_ATTRIBUTE / Opcodes.ACC_SYNTHETIC));
        out.putShort(access & ~mask).putShort(name).putShort(desc);
        int attributeCount = 0;
        if (value != 0) {
            ++attributeCount;
        }
        if ((access & Opcodes.ACC_SYNTHETIC) != 0
                && ((cw.version & 0xFFFF) < Opcodes.V1_5 || (access & ClassWriter.ACC_SYNTHETIC_ATTRIBUTE) != 0))
        {
            ++attributeCount;
        }
        if ((access & Opcodes.ACC_DEPRECATED) != 0) {
            ++attributeCount;
        }
        if (ClassReader.SIGNATURES && signature != 0) {
            ++attributeCount;
        }
        if (ClassReader.ANNOTATIONS && anns != null) {
            ++attributeCount;
        }
        if (ClassReader.ANNOTATIONS && ianns != null) {
            ++attributeCount;
        }
        if (attrs != null) {
            attributeCount += attrs.getCount();
        }
        out.putShort(attributeCount);
        if (value != 0) {
            out.putShort(cw.newUTF8("ConstantValue"));
            out.putInt(2).putShort(value);
        }
        if ((access & Opcodes.ACC_SYNTHETIC) != 0
                && ((cw.version & 0xFFFF) < Opcodes.V1_5 || (access & ClassWriter.ACC_SYNTHETIC_ATTRIBUTE) != 0))
        {
            out.putShort(cw.newUTF8("Synthetic")).putInt(0);
        }
        if ((access & Opcodes.ACC_DEPRECATED) != 0) {
            out.putShort(cw.newUTF8("Deprecated")).putInt(0);
        }
        if (ClassReader.SIGNATURES && signature != 0) {
            out.putShort(cw.newUTF8("Signature"));
            out.putInt(2).putShort(signature);
        }
        if (ClassReader.ANNOTATIONS && anns != null) {
            out.putShort(cw.newUTF8("RuntimeVisibleAnnotations"));
            anns.put(out);
        }
        if (ClassReader.ANNOTATIONS && ianns != null) {
            out.putShort(cw.newUTF8("RuntimeInvisibleAnnotations"));
            ianns.put(out);
        }
        if (attrs != null) {
            attrs.put(cw, null, 0, -1, -1, out);
        }
    }
}
