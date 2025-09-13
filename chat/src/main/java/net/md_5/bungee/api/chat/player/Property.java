package net.md_5.bungee.api.chat.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class Property
{

    @NonNull
    private String name;
    @NonNull
    private String value;
    private String signature;

    public Property(@NonNull String name, @NonNull String value)
    {
        this( name, value, null );
    }
}
