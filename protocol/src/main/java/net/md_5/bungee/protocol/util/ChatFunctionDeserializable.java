package net.md_5.bungee.protocol.util;

import java.util.function.Function;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.nbt.TypedTag;
import org.jetbrains.annotations.NotNull;

public class ChatFunctionDeserializable extends ChatCapturingDeserializable
{
    private final Function<Either<String, TypedTag>, BaseComponent> function;

    public ChatFunctionDeserializable(Either<String, TypedTag> ov, Function<Either<String, TypedTag>, BaseComponent> supplier)
    {
        super( ov );
        this.function = supplier;
    }

    @NotNull
    @Override
    public BaseComponent deserialize()
    {
        return function.apply( original() );
    }
}
