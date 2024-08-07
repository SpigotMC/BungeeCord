package net.md_5.bungee.protocol.util;

import net.md_5.bungee.api.chat.BaseComponent;
import se.llbit.nbt.SpecificTag;

class ChatNewOldAdapter implements ChatDeserializable
{

    private final ChatNewDeserializable newDeserializable;

    public ChatNewOldAdapter(ChatNewDeserializable newDeserializable)
    {
        this.newDeserializable = newDeserializable;
    }

    @Override
    public BaseComponent get()
    {
        return newDeserializable.get();
    }

    @Override
    public Either<String, SpecificTag> original()
    {
        return Either.right( newDeserializable.original() );
    }

    @Override
    public boolean hasDeserialized()
    {
        return newDeserializable.hasDeserialized();
    }
}
