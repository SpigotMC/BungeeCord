package net.md_5.bungee.api.connection;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ServerConnectRequest;
import net.md_5.bungee.api.SkinConfiguration;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.score.Scoreboard;

/**
 * Represents a player whose connection is being connected to somewhere else,
 * whether it be a remote or embedded server.
 */
public interface ProxiedPlayer extends Connection, CommandSender
{

    /**
     * Represents the player's chat state.
     */
    public enum ChatMode
    {

        /**
         * The player will see all chat.
         */
        SHOWN,
        /**
         * The player will only see everything except messages marked as chat.
         */
        COMMANDS_ONLY,
        /**
         * The chat is completely disabled, the player won't see anything.
         */
        HIDDEN;

    }

    public enum MainHand
    {

        LEFT,
        RIGHT;
    }

    /**
     * Gets this player's display name.
     *
     * @return the players current display name
     */
    String getDisplayName();

    /**
     * Sets this player's display name to be used by proxy commands and plugins.
     *
     * @param name the name to set
     */
    void setDisplayName(String name);

    /**
     * Send a message to the specified screen position of this player.
     *
     * @param position the screen position
     * @param message the message to send
     */
    public void sendMessage(ChatMessageType position, BaseComponent... message);

    /**
     * Send a message to the specified screen position of this player.
     *
     * @param position the screen position
     * @param message the message to send
     */
    public void sendMessage(ChatMessageType position, BaseComponent message);

    /**
     * Send a message to this player.
     *
     * @param sender the sender of the message
     * @param message the message to send
     */
    public void sendMessage(UUID sender, BaseComponent... message);

    /**
     * Send a message to this player.
     *
     * @param sender the sender of the message
     * @param message the message to send
     */
    public void sendMessage(UUID sender, BaseComponent message);

    /**
     * Connects / transfers this user to the specified connection, gracefully
     * closing the current one. Depending on the implementation, this method
     * might return before the user has been connected.
     *
     * @param target the new server to connect to
     */
    void connect(ServerInfo target);

    /**
     * Connects / transfers this user to the specified connection, gracefully
     * closing the current one. Depending on the implementation, this method
     * might return before the user has been connected.
     *
     * @param target the new server to connect to
     * @param reason the reason for connecting to the new server
     */
    void connect(ServerInfo target, ServerConnectEvent.Reason reason);

    /**
     * Connects / transfers this user to the specified connection, gracefully
     * closing the current one. Depending on the implementation, this method
     * might return before the user has been connected.
     *
     * @param target the new server to connect to
     * @param callback the method called when the connection is complete, or
     * when an exception is encountered. The boolean parameter denotes success
     * (true) or failure (false).
     */
    void connect(ServerInfo target, Callback<Boolean> callback);

    /**
     * Connects / transfers this user to the specified connection, gracefully
     * closing the current one. Depending on the implementation, this method
     * might return before the user has been connected.
     *
     * @param target the new server to connect to
     * @param callback the method called when the connection is complete, or
     * when an exception is encountered. The boolean parameter denotes success
     * (true) or failure (false).
     * @param reason the reason for connecting to the new server
     */
    void connect(ServerInfo target, Callback<Boolean> callback, ServerConnectEvent.Reason reason);

    /**
     * Connects / transfers this user to the specified connection, gracefully
     * closing the current one. Depending on the implementation, this method
     * might return before the user has been connected.
     *
     * @param request request to connect with
     */
    void connect(ServerConnectRequest request);

    /**
     * Gets the server this player is connected to.
     *
     * @return the server this player is connected to
     */
    Server getServer();

    /**
     * Gets the ping time between the proxy and this connection.
     *
     * @return the current ping time
     */
    int getPing();

    /**
     * Send a plugin message to this player.
     *
     * In recent Minecraft versions channel names must contain a colon separator
     * and consist of [a-z0-9/._-]. This will be enforced in a future version.
     * The "BungeeCord" channel is an exception and may only take this form.
     *
     * @param channel the channel to send this data via
     * @param data the data to send
     */
    void sendData(String channel, byte[] data);

    /**
     * Get the pending connection that belongs to this player.
     *
     * @return the pending connection that this player used
     */
    PendingConnection getPendingConnection();

    /**
     * Make this player chat (say something), to the server he is currently on.
     *
     * @param message the message to say
     */
    void chat(String message);

    /**
     * Get the server which this player will be sent to next time the log in.
     *
     * @return the server, or null if default
     */
    ServerInfo getReconnectServer();

    /**
     * Set the server which this player will be sent to next time the log in.
     *
     * @param server the server to set
     */
    void setReconnectServer(ServerInfo server);

    /**
     * Get this connection's UUID, if set.
     *
     * @return the UUID
     * @deprecated In favour of {@link #getUniqueId()}
     */
    @Deprecated
    String getUUID();

    /**
     * Get this connection's UUID, if set.
     *
     * @return the UUID
     */
    UUID getUniqueId();

    /**
     * Gets this player's locale.
     *
     * @return the locale
     */
    Locale getLocale();

    /**
     * Gets this player's view distance.
     *
     * @return the view distance, or a reasonable default
     */
    byte getViewDistance();

    /**
     * Gets this player's chat mode.
     *
     * @return the chat flags set, or a reasonable default
     */
    ChatMode getChatMode();

    /**
     * Gets if this player has chat colors enabled or disabled.
     *
     * @return if chat colors are enabled
     */
    boolean hasChatColors();

    /**
     * Gets this player's skin settings.
     *
     * @return the players skin setting
     */
    SkinConfiguration getSkinParts();

    /**
     * Gets this player's main hand setting.
     *
     * @return main hand setting
     */
    MainHand getMainHand();

    /**
     * Set the header and footer displayed in the tab player list.
     *
     * @param header The header for the tab player list, null to clear it.
     * @param footer The footer for the tab player list, null to clear it.
     */
    void setTabHeader(BaseComponent header, BaseComponent footer);

    /**
     * Set the header and footer displayed in the tab player list.
     *
     * @param header The header for the tab player list, null to clear it.
     * @param footer The footer for the tab player list, null to clear it.
     */
    void setTabHeader(BaseComponent[] header, BaseComponent[] footer);

    /**
     * Clears the header and footer displayed in the tab player list.
     */
    void resetTabHeader();

    /**
     * Sends a {@link Title} to this player. This is the same as calling
     * {@link Title#send(ProxiedPlayer)}.
     *
     * @param title The title to send to the player.
     * @see Title
     */
    void sendTitle(Title title);

    /**
     * Gets whether this player is using a FML client.
     * <p>
     * This method is only reliable if BungeeCord links Minecraft 1.8 servers
     * together, as Bungee can pick up whether a user is a Forge user with the
     * initial handshake. If this is used for a 1.7 network, this might return
     * <code>false</code> even if the user is a FML user, as Bungee can only
     * determine this information if a handshake successfully completes.
     * </p>
     *
     * @return <code>true</code> if it is known that the user is using a FML
     * client, <code>false</code> otherwise.
     */
    boolean isForgeUser();

    /**
     * Gets this player's Forge Mod List, if the player has sent this
     * information during the lifetime of their connection to Bungee. There is
     * no guarantee that information is available at any time, as it is only
     * sent during a FML handshake. Therefore, this will only contain
     * information for a user that has attempted joined a Forge server.
     * <p>
     * Consumers of this API should be aware that an empty mod list does
     * <em>not</em> indicate that a user is not a Forge user, and so should not
     * use this API to check for this. See the {@link #isForgeUser()
     * isForgeUser} method instead.
     * </p>
     * <p>
     * Calling this when handling a
     * {@link net.md_5.bungee.api.event.ServerConnectedEvent} may be the best
     * place to do so as this event occurs after a FML handshake has completed,
     * if any has occurred.
     * </p>
     *
     * @return A {@link Map} of mods, where the key is the name of the mod, and
     * the value is the version. Returns an empty list if the FML handshake has
     * not occurred for this {@link ProxiedPlayer} yet.
     */
    Map<String, String> getModList();

    /**
     * Get the {@link Scoreboard} that belongs to this player.
     *
     * @return this player's {@link Scoreboard}
     */
    Scoreboard getScoreboard();
}
