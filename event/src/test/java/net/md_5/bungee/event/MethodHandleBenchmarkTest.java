package net.md_5.bungee.event;

import net.md_5.bungee.event.bench.MethodHandleBenchmark;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class MethodHandleBenchmarkTest
{
    @Test
    public void startBenchmark() throws Exception
    {
        Options opt = new OptionsBuilder().include( MethodHandleBenchmark.class.getSimpleName() ).build();
        new Runner( opt ).run();
    }
}
