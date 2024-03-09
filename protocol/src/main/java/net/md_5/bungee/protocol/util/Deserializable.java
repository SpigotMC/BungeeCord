package net.md_5.bungee.protocol.util;

/**
 * Represents a value that can be deserialized from another value if needed.
 * @param <OV> the original value
 * @param <D> the deserialized value
 */
public interface Deserializable<OV, D>
{
    /**
     * @return the deserialized value
     */
    D get();

    /**
     * If {@link #hasDeserialized()} returns true, this method may return null. This usually hapens after code has
     * edited the deserialized value and wrote it back to its original place.
     * @return the original value, if available
     */
    OV original();

    /**
     * If the value has been deserialized, it is adviced to no longer call {@link #original()}, as the deserialized
     * value may have been modified.
     * @return true if the value has been deserialized
     */
    boolean hasDeserialized();
}
