package net.md_5.bungee.api.connection;

import java.util.Locale;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.config.ServerInfo;
import java.util.UUID;

/**
 * Represents a player who's connection is being connected to somewhere else,
 * whether it be a remote or embedded server.
 */
public interface ProxiedPlayer extends Connection, CommandSender
{

    /**
     * Gets this player's display name.
     *
     * @return the players current display name
     */
    String getDisplayName();

    /**
     * Sets this players display name to be used as their nametag and tab list
     * name.
     *
     * @param name the name to set
     */
    void setDisplayName(String name);

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
     * @param callback the method called when the connection is complete, or
     * when an exception is encountered. The boolean parameter denotes success
     * or failure.
     */
    void connect(ServerInfo target, Callback<Boolean> callback);

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
     * Set the header and footer displayed in the tab player list.
     * @param header The header for the tab player list.
     * @param footer The footer for the tab player list.
     */
    void setTabHeader(BaseComponent header, BaseComponent footer);

    /**
     * Set the header and footer displayed in the tab player list.
     * @param header The header for the tab player list.
     * @param footer The footer for the tab player list.
     */
    void setTabHeader(BaseComponent[] header, BaseComponent[] footer);

    /**
     * Send a title to the player using the previously used subtitle and display times.
     * It will be displayed in the center of the player screen.
     * You should call {@link #resetTitle()} first if your want your title
     * to be displayed correctly with the default settings.
     * @param title The title to display.
     */
    void sendTitle(BaseComponent title);

    /**
     * Send a title to the player using the previously used subtitle and display times.
     * It will be displayed in the center of the player screen.
     * You should call {@link #resetTitle()} first if your want your title
     * to be displayed correctly with the default settings.
     * @param title The title to display.
     */
    void sendTitle(BaseComponent... title);

    /**
     * Send a title and a subtitle to the player using the previously
     * used display times.
     * It will be displayed in the center of the player screen.
     * You should call {@link #resetTitle()} first if your want your title
     * to be displayed correctly with the default settings.
     * @param title The title to display.
     * @param subtitle The subtitle to display.
     */
    void sendTitle(BaseComponent title, BaseComponent subtitle);

    /**
     * Send a title and a subtitle to the player using the previously used display times.
     * It will be displayed in the center of the player screen.
     * You should call {@link #resetTitle()} first if your want your title
     * to be displayed correctly with the default settings.
     * @param title The title to display.
     * @param subtitle The subtitle to display.
     */
    void sendTitle(BaseComponent[] title, BaseComponent[] subtitle);

    /**
     * Send a title and a subtitle to the player with specified fade in, stay and fade out times.
     * It will be displayed in the center of the player screen.
     * @param title The title to display.
     * @param subtitle The subtitle to display.
     * @param fadeIn The time for the fade in effect, in ticks.
     * @param stay How long the title should stay on the player's screen, in ticks.
     * @param fadeOut The time for the fade out effect, in ticks.
     */
    void sendTitle(BaseComponent title, BaseComponent subtitle, int fadeIn, int stay, int fadeOut);

    /**
     * Send a title and a subtitle to the player with specified fade in, stay and fade out times.
     * It will be displayed in the center of the player screen.
     * @param title The title to display.
     * @param subtitle The subtitle to display.
     * @param fadeIn The time for the fade in effect, in ticks.
     * @param stay How long the title should stay on the player's screen, in ticks.
     * @param fadeOut The time for the fade out effect, in ticks.
     */
    void sendTitle(BaseComponent[] title, BaseComponent[] subtitle, int fadeIn, int stay, int fadeOut);

    /**
     * Remove the title from the player's screen, but keep the last used display times.
     */
    void clearTitle();


    /**
     * Remove the currently displayed title from the player's screen and reset
     * the display times to the defaults.
     */
    void resetTitle();
}
