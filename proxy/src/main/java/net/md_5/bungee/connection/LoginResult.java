package net.md_5.bungee.connection;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResult
{

    private String id;
    private String name;
    private Property[] properties;
 
    public LoginResult(String id, Property[] properties) {
        this(id, null, properties);
    }

    @Data
    @AllArgsConstructor
    public static class Property
    {

        private String name;
        private String value;
        private String signature;
    }
}
