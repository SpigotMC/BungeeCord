package net.md_5.bungee.protocol.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.nbt.TypedTag;

@RequiredArgsConstructor
public abstract class ChatCapturingDeserializable implements ChatDeserializable
{
    private final Either<String, TypedTag> original;
    private BaseComponent deserialized;

    /**
     * Method called to get the deserialized value. Called only once unless multiple threads are calling get() at the
     * same time.
     * @return the deserialized value
     */
    @NonNull
    protected abstract BaseComponent deserialize();

    @Override
    public final BaseComponent get()
    {
        if ( !hasDeserialized() )
        {
            return deserialized = deserialize();
        }
        return deserialized;
    }

    @Override
    public final boolean hasDeserialized()
    {
        return deserialized != null;
    }

    @Override
    public final Either<String, TypedTag> original()
    {
        return original;
    }
}
