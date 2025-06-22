package net.md_5.bungee.connection;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.md_5.bungee.protocol.Property;

@Data
@AllArgsConstructor
public class LoginResult
{

    public static final Gson GSON = new Gson();
    //
    private String id;
    private String name;
    private Property[] properties;
}
