package net.md_5.bungee.api;

import java.util.Collection;
import net.md_5.bungee.api.chat.BaseComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

public interface CommandSender
{

    /**
     * Get the unique name of this command sender.
     *
     * @return the senders username
     */
    @NotNull
    @Contract(pure = true)
    public String getName();

    /**
     * Send a message to this sender.
     *
     * @param message the message to send
     */
    @Deprecated
    public void sendMessage(@NotNull String message);

    /**
     * Send several messages to this sender. Each message will be sent
     * separately.
     *
     * @param messages the messages to send
     */
    @Deprecated
    public void sendMessages(@NotNull String... messages);

    /**
     * Send a message to this sender.
     *
     * @param message the message to send
     */
    public void sendMessage(@NotNull BaseComponent... message);

    /**
     * Send a message to this sender.
     *
     * @param message the message to send
     */
    public void sendMessage(@NotNull BaseComponent message);

    /**
     * Get all groups this user is part of. This returns an unmodifiable
     * collection.
     *
     * @return the users groups
     */
    @NotNull
    @UnmodifiableView
    public Collection<String> getGroups();

    /**
     * Adds groups to a this user for the current session only.
     *
     * @param groups the groups to add
     */
    public void addGroups(@NotNull String... groups);

    /**
     * Remove groups from this user for the current session only.
     *
     * @param groups the groups to remove
     */
    public void removeGroups(@NotNull String... groups);

    /**
     * Checks if this user has the specified permission node.
     *
     * @param permission the node to check
     * @return whether they have this node
     */
    public boolean hasPermission(@NotNull String permission);

    /**
     * Set a permission node for this user.
     *
     * @param permission the node to set
     * @param value the value of the node
     */
    public void setPermission(@NotNull String permission, boolean value);

    /**
     * Get all Permissions which this CommandSender has
     *
     * @return a unmodifiable Collection of Strings which represent their
     * permissions
     */
    @NotNull
    @UnmodifiableView
    public Collection<String> getPermissions();
}
