package net.md_5.bungee.api.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.AbstractEvent;

/**
 * Called when somebody reloads BungeeCord
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ProxyReloadEvent extends AbstractEvent
{

    /**
     * Creator of the action.
     */
    private final CommandSender sender;
}
