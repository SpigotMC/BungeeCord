package net.md_5.bungee.api;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;

/**
 * A request to connect a server.
 */
@Getter
@Builder(builderClassName = "Builder")
public class ServerConnectRequest
{

    /**
     * The result from this callback after request has been executed by proxy.
     */
    public enum Result
    {

        /**
         * ServerConnectEvent to the new server was canceled.
         */
        EVENT_CANCEL,
        /**
         * Already connected to target server.
         */
        ALREADY_CONNECTED,
        /**
         * Already connecting to target server.
         */
        ALREADY_CONNECTING,
        /**
         * Successfully connected to server.
         */
        SUCCESS,
        /**
         * Connection failed, error can be accessed from callback method handle.
         */
        FAIL,
        /**
         * User has no access to this server.
         */
        UNAUTHORISED
    }

    /**
     * Target server to connect to.
     */
    @NonNull
    private final ServerInfo target;
    /**
     * Reason for connecting to server.
     */
    @NonNull
    private final ServerConnectEvent.Reason reason;
    /**
     * Callback to execute post request.
     */
    private final Callback<Result> callback;
    /**
     * Timeout in milliseconds for request.
     */
    @Setter
    private int connectTimeout;
    /**
     * Should the player be attempted to connect to the next server in their
     * queue if the initial request fails.
     */
    @Setter
    private boolean retry;
    /**
     * Disabled by default. Set to true if the the permissions defined in the config
     * to restrict servers to certain players should be respected for this request.
     */
    @Setter
    private boolean requireAccess;

    /**
     * Class that sets default properties/adds methods to the lombok builder
     * generated class.
     */
    public static class Builder
    {

        private boolean requireAccess = ProxyServer.getInstance().getConfig().isRequireAccess();
        private int connectTimeout = ProxyServer.getInstance().getConfig().getServerConnectTimeout();
    }
}
