package net.md_5.bungee.protocol.util;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.md_5.bungee.api.chat.BaseComponent;
import se.llbit.nbt.SpecificTag;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class ChatNewComponentDeserializable implements ChatNewDeserializable
{
    private final BaseComponent value;

    @Override
    public BaseComponent get()
    {
        return value;
    }

    @Override
    public SpecificTag original()
    {
        return null;
    }

    @Override
    public boolean hasDeserialized()
    {
        return true;
    }
}
