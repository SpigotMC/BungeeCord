package net.md_5.bungee.protocol.util;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.nbt.TypedTag;

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
    public Either<String, TypedTag> original()
    {
        return null;
    }

    @Override
    public boolean hasDeserialized()
    {
        return true;
    }
}
