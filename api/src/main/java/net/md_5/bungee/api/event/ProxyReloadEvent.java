package net.md_5.bungee.api.event;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called when somebody reloads BungeeCord
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProxyReloadEvent extends Event
{

    /**
     * Creator of the action.
     */
    private final CommandSender sender;
}
