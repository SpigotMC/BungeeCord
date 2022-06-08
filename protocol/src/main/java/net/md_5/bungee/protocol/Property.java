package net.md_5.bungee.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Property
{

    private String name;
    private String value;
    private String signature;

    public Property(String name, String value)
    {
        this( name, value, null );
    }
}
