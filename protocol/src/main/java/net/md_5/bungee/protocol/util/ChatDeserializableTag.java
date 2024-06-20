package net.md_5.bungee.protocol.util;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.VersionedComponentSerializer;
import net.md_5.bungee.nbt.TypedTag;
import net.md_5.bungee.protocol.ChatSerializer;
import org.jetbrains.annotations.NotNull;

@ToString
@EqualsAndHashCode(callSuper = true)
public class ChatDeserializableTag extends ChatCapturingDeserializable
{
    private final @NonNull VersionedComponentSerializer serializer;

    public ChatDeserializableTag(int protocolVersion, @NonNull TypedTag chatTag)
    {
        this( ChatSerializer.forVersion( protocolVersion ), chatTag );
    }

    public ChatDeserializableTag(@NonNull VersionedComponentSerializer serializer, @NonNull TypedTag chatTag)
    {
        super( Either.right( chatTag ) );
        this.serializer = serializer;
    }

    @NotNull
    @Override
    public BaseComponent deserialize()
    {
        return serializer.deserialize( TagUtil.toJson( original().getRight() ) );
    }
}
