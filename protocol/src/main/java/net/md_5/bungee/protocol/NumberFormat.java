package net.md_5.bungee.protocol;

import lombok.Data;

@Data
public class NumberFormat
{

    private final Type type;
    private final Object value;

    public enum Type
    {
        BLANK,
        STYLED,
        FIXED;
    }
}
