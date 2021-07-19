package net.md_5.bungee.api.event;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.profile.ProfileProperty;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event, called after a player profile complete (profile fetched from mojang api if
 * online-mode is true, or if offline mode after is created).
 */
@Getter
@AllArgsConstructor
public class PlayerProfileCompleteEvent extends Event
{

    /**
     * The player for which the profile's being modified.
     */
    private final ProxiedPlayer player;

    /**
     * The properties of the player profile.
     */
    private Set<ProfileProperty> properties;

    /**
     * Returns whenever the profile contains the specified property.
     *
     * @param property the property you want to check if is present
     * @return <code>true</code> if present, <code>false</code> otherwise
     */
    public boolean hasProperty(ProfileProperty property)
    {
        return properties.contains( property );
    }

    /**
     * Returns whenever the profile contains the property with the name specified.
     *
     * @param propertyName the property's name you want to check if is present
     * @return <code>true</code> if present, <code>false</code> otherwise
     */
    public boolean hasProperty(String propertyName)
    {
        return properties.stream().anyMatch( property -> property.getName().equalsIgnoreCase( propertyName ) );
    }

    /**
     * Adds the specified property to the profile.
     *
     * @param property property you want to add
     */
    public void addProperty(ProfileProperty property)
    {
        properties.add( property );
    }

    /**
     * Removes the specified property from the profile if present.
     *
     * @param property property you want to remove
     */
    public void removeProperty(ProfileProperty property)
    {
        properties.remove( property );
    }

    /**
     * Removes the property with the specified name if present.
     *
     * @param propertyName the name of the property you want to remove
     */
    public void removeProperty(String propertyName)
    {
        properties.stream()
            .filter( property -> property.getName().equalsIgnoreCase( propertyName ) )
            .findFirst()
            .ifPresent( this::removeProperty );
    }
}
