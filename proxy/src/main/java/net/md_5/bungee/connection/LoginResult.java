package net.md_5.bungee.connection;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.md_5.bungee.api.connection.profile.ProfileProperty;

@Data
@AllArgsConstructor
public class LoginResult
{

    private String id;
    private String name;
    private Set<ProfileProperty> properties;
}
