package net.md_5.bungee.connection;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.md_5.bungee.protocol.Property;

@Data
@AllArgsConstructor
public class LoginResult
{

    private String id;
    private String name;
    private Property[] properties;
}
