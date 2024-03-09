package net.md_5.bungee.protocol.util;

import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public class FunctionDeserializable<OV, D> extends SimpleDeserializable<OV, D>
{
    private final Function<OV, D> function;

    public FunctionDeserializable(OV ov, Function<OV, D> supplier)
    {
        super( ov );
        this.function = supplier;
    }

    @NotNull
    @Override
    public D deserialize()
    {
        return function.apply( original() );
    }
}
