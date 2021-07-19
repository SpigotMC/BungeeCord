package net.md_5.bungee.api.connection.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a property of a player profile.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileProperty
{

    /**
     * The name of the property.
     */
    private String name;

    /**
     * The value of the property. Usually a base64 string.
     */
    private String value;

    /**
     * The signature of the property. A base64 string, signed using Yggdrasil's
     * private key.
     */
    private String signature;
}
