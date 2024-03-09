package net.md_5.bungee.protocol.util;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NoOrigDeserializable<OV, D> implements Deserializable<OV, D>
{
    private final D value;

    @Override
    public D get()
    {
        return value;
    }

    @Override
    public OV original()
    {
        return null;
    }

    @Override
    public boolean hasDeserialized()
    {
        return true;
    }
}
