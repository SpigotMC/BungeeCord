package net.md_5.bungee.protocol.util;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.VersionedComponentSerializer;
import net.md_5.bungee.protocol.ChatSerializer;
import org.jetbrains.annotations.NotNull;

@ToString
@EqualsAndHashCode(callSuper = true)
public class ChatDeserializableJson extends ChatCapturingDeserializable
{

    private final @NonNull VersionedComponentSerializer serializer;

    public ChatDeserializableJson(int protocolVersion, @NonNull String chatJson)
    {
        this( ChatSerializer.forVersion( protocolVersion ), chatJson );
    }

    public ChatDeserializableJson(@NonNull VersionedComponentSerializer serializer, @NonNull String chatJson)
    {
        super( Either.left( chatJson ) );
        this.serializer = serializer;
    }

    @NotNull
    @Override
    public BaseComponent deserialize()
    {
        return serializer.deserialize( original().getLeft() );
    }
}
