package net.md_5.bungee.event;

import java.util.Arrays;
import java.util.Collection;
import lombok.Getter;
import net.md_5.bungee.event.asm.CompiledEventBus;
import net.md_5.bungee.event.standard.StandardEventBus;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(value = Parameterized.class)
public class ImplementationRegistry
{

    @Getter
    protected final EventBus bus;

    public ImplementationRegistry(EventBus bus)
    {
        this.bus = bus;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getBusList()
    {
        return Arrays.asList( new Object[][]
        {
            {
                new StandardEventBus()
            },/*
            {
                new CompiledEventBus()
            }*/
        } );
    }
}
