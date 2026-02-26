package net.md_5.bungee.event.bench;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 1)
public class MethodHandleBenchmark
{

    @Data
    public static class TestEvent
    {
        private final Blackhole blackhole;
    }

    public static class TestListener
    {
        public void onEvent(TestEvent e)
        {
            e.blackhole.consume( 1 );
        }
    }

    // Variables we need
    private TestListener listener;
    private Method reflectionMethod;
    private MethodHandle methodHandle;

    @Setup
    public void setup() throws Throwable
    {
        listener = new TestListener();

        // Setup Reflection
        reflectionMethod = TestListener.class.getMethod( "onEvent", TestEvent.class );

        // Setup MethodHandle
        methodHandle = MethodHandles.lookup().unreflect( reflectionMethod ).bindTo( listener ).asType( MethodType.methodType( void.class, Object.class ) );
    }

    @Benchmark
    public void testDirectCall(Blackhole bh)
    {
        listener.onEvent( new TestEvent( bh ) );
    }

    @Benchmark
    public void testReflection(Blackhole bh) throws Exception
    {
        reflectionMethod.invoke( listener, new TestEvent( bh ) );
    }

    @Benchmark
    public void testMethodHandle(Blackhole bh) throws Throwable
    {
        methodHandle.invokeExact( (Object) new TestEvent( bh ) );
    }
}
