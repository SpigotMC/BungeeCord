package net.md_5.bungee.api.event;

import java.util.function.BiConsumer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

/**
 * Event, called when {@link ProxiedPlayer#setPermission(String, boolean)} was called.
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.NONE)
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class PermissionSetEvent extends Event
{

    /**
     * The {@link ProxiedPlayer} on which the setPermission was triggered.
     */
    private final ProxiedPlayer player;

    /**
     * The permission set function, called in order to set the player's permission.
     */
    private BiConsumer<String, Boolean> permissionSetFunction;
}
