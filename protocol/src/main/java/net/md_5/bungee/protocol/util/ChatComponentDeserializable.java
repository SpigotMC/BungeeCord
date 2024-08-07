package net.md_5.bungee.protocol.util;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.md_5.bungee.api.chat.BaseComponent;
import se.llbit.nbt.SpecificTag;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class ChatComponentDeserializable implements ChatDeserializable
{
    private final BaseComponent value;

    @Override
    public BaseComponent get()
    {
        return value;
    }

    @Override
    public Either<String, SpecificTag> original()
    {
        return null;
    }

    @Override
    public boolean hasDeserialized()
    {
        return true;
    }
}
