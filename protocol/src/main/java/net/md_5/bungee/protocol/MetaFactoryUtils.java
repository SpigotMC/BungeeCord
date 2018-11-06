package net.md_5.bungee.protocol;

import com.google.common.base.Supplier;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;

public class MetaFactoryUtils
{

    public static Supplier<? extends DefinedPacket> createNoArgsConstructorUnchecked(Class<? extends DefinedPacket> packetClazz)
    {
        try
        {
            return createNoArgsConstructor( packetClazz );
        } catch ( NoSuchMethodError e )
        {
            throw new BadPacketException( "Can not construct packet with class name " + packetClazz.getName(), e );
        } catch ( Throwable throwable )
        {
            throw new RuntimeException( throwable );
        }
    }

    @SuppressWarnings("unchecked")
    private static Supplier<? extends DefinedPacket> createNoArgsConstructor(Class<? extends DefinedPacket> packetClazz) throws Throwable
    {
        MethodHandles.Lookup caller = constructLookup( packetClazz );
        MethodType invokedType = MethodType.methodType( Supplier.class );
        CallSite site = LambdaMetafactory.metafactory( caller,
                "get",
                invokedType,
                MethodType.methodType( Object.class ),
                MethodHandles.lookup().findConstructor( packetClazz, MethodType.methodType( void.class ) ),
                MethodType.methodType( Object.class ) );
        MethodHandle factory = site.getTarget();
        return (Supplier<? extends DefinedPacket>) factory.invoke();
    }

    private static MethodHandles.Lookup constructLookup(Class<?> owner) throws Exception
    {
        Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor( Class.class );
        constructor.setAccessible( true );
        try
        {
            return constructor.newInstance( owner );
        } finally
        {
            constructor.setAccessible( false );
        }
    }
}
