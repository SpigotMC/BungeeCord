package net.md_5.bungee.protocol.util;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.jetbrains.annotations.NotNull;

@ToString
@EqualsAndHashCode(callSuper = true)
public class ChatDeserializableJson extends ChatCapturingDeserializable
{

    public ChatDeserializableJson(@NonNull String chatJson)
    {
        super( Either.left( chatJson ) );
    }

    @NotNull
    @Override
    public BaseComponent deserialize()
    {
        return ComponentSerializer.deserialize( original().getLeft() );
    }
}
