package net.md_5.bungee.protocol.util;

import java.util.function.Function;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.nbt.TypedTag;
import org.jetbrains.annotations.NotNull;

@ToString
@EqualsAndHashCode(callSuper = true)
public class ChatFunctionDeserializable extends ChatCapturingDeserializable
{
    private final Function<Either<String, TypedTag>, BaseComponent> function;

    public ChatFunctionDeserializable(@NonNull Either<String, TypedTag> ov, Function<Either<String, TypedTag>, BaseComponent> supplier)
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
