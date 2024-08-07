package net.md_5.bungee.protocol.util;

import net.md_5.bungee.api.chat.BaseComponent;
import se.llbit.nbt.SpecificTag;

public interface ChatDeserializable extends Deserializable<Either<String, SpecificTag>, BaseComponent>
{
    default ChatNewDeserializable cloneAsNew()
    {
        return new ChatNewComponentDeserializable( get() );
    }
}
