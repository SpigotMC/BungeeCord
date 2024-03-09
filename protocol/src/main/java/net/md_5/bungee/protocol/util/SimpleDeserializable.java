package net.md_5.bungee.protocol.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class SimpleDeserializable<OV, D> implements Deserializable<OV, D>
{
    private final OV original;
    private D deserialized;

    /**
     * Method called to get the deserialized value. Called only once unless multiple threads are calling get() at the
     * same time.
     * @return the deserialized value
     */
    @NonNull
    protected abstract D deserialize();

    @Override
    public final D get()
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
    public final OV original()
    {
        return original;
    }
}
