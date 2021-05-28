package net.md_5.bungee.util;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public final class LookupFetcherClassCreator implements Opcodes
{
    private static final AtomicInteger counter = new AtomicInteger( 1 );

    private static final String OBJECT_INAME = Type.getInternalName( Object.class );
    private static final String SUPPLIER_INAME = Type.getInternalName( Supplier.class );
    private static final String SUPPLIER_GET_NAME;
    private static final String SUPPLIER_METHOD_DESCRIPTOR;
    private static final String METHODHANDLES_INAME = Type.getInternalName( MethodHandles.class );
    private static final String LOOKUP_METHOD_DESCRIPTOR;
    private static final String LOOKUP_NAME;

    static
    {
        Method supplierGet;
        Method methodHandlesLookup;
        try
        {
            supplierGet = Supplier.class.getMethod( "get" );
            methodHandlesLookup = MethodHandles.class.getMethod( "lookup" );
        } catch ( NoSuchMethodException ex )
        {
            throw new RuntimeException( ex );
        }
        SUPPLIER_METHOD_DESCRIPTOR = Type.getMethodDescriptor( supplierGet );
        LOOKUP_METHOD_DESCRIPTOR = Type.getMethodDescriptor( methodHandlesLookup );
        SUPPLIER_GET_NAME = supplierGet.getName();
        LOOKUP_NAME = methodHandlesLookup.getName();
    }

    public LookupFetcherClassCreator()
    {
        throw new UnsupportedOperationException();
    }

    public static AbstractMap.SimpleEntry<String, byte[]> create()
    {
        ClassWriter cw = new ClassWriter( 0 );
        int i = counter.getAndIncrement();
        String name = "net/md_5/bungee/generated/PluginAccessor" + i;
        cw.visit(
                V1_8,
                ACC_PUBLIC | ACC_SUPER | ACC_FINAL,
                name,
                null,
                OBJECT_INAME,
                new String[]{SUPPLIER_INAME}
        );
        cw.visitSource( "PluginAccessor" + i + ".java", null );

        MethodVisitor mv = cw.visitMethod( ACC_PUBLIC, "<init>", "()V", null, null );
        mv.visitVarInsn( ALOAD, 0 );
        mv.visitMethodInsn( INVOKESPECIAL, OBJECT_INAME, "<init>", "()V", false );
        mv.visitInsn( RETURN );
        mv.visitMaxs( 1, 1 );
        mv.visitEnd();

        mv = cw.visitMethod( ACC_PUBLIC | ACC_FINAL, SUPPLIER_GET_NAME, SUPPLIER_METHOD_DESCRIPTOR, null, null );
        mv.visitMethodInsn( INVOKESTATIC, METHODHANDLES_INAME, LOOKUP_NAME, LOOKUP_METHOD_DESCRIPTOR, false );
        mv.visitInsn( ARETURN );
        mv.visitMaxs( 1, 1 );
        mv.visitEnd();

        cw.visitEnd();

        return new AbstractMap.SimpleEntry<>( name.replace( '/', '.' ), cw.toByteArray() );
    }
}
