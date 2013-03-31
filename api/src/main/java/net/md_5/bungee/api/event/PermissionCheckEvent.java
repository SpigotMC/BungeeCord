package net.md_5.bungee.api.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called when the permission of a CommandSender is checked.
 */
@Data
@AllArgsConstructor
@ToString(callSuper = false)
@EqualsAndHashCode(callSuper = false)
public class PermissionCheckEvent extends Event
{

    /**
     * The command sender being checked for a permission.
     */
    private final CommandSender sender;
    /**
     * The permission to check.
     */
    private final String permission;
    /**
     * The outcome of this permission check.
     */
    @Getter(AccessLevel.NONE)
    private boolean hasPermission;

    public boolean hasPermission()
    {
        return hasPermission;
    }
}
