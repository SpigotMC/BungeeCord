package net.md_5.bungee.protocol.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.nbt.TypedTag;

public interface ChatDeserializable extends Deserializable<Either<String, TypedTag>, BaseComponent>
{
    default ChatNewDeserializable cloneAsNew()
    {
        return new ChatNewComponentDeserializable( get() );
    }
}
