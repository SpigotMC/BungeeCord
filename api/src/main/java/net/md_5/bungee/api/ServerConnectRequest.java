package net.md_5.bungee.api;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;

/**
 * A request to connect a server
 */
@Getter
@Builder
public class ServerConnectRequest
{

    /**
     * The result from this callback after request has been executed by proxy.
     */
    public enum Result {

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
        FAIL
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
    private final int connectTimeout;
    /**
     * Should the player be attempted to connect to the next server
     * in their queue if the initial request fails.
     */
    private final boolean retry;

    /**
     * Class that sets default properties/adds methods to the lombok builder generated class.
     */
    public static class ServerConnectRequestBuilder
    {

        private Callback<Result> callback;
        private int connectTimeout = 5000; // TODO: Configurable

        /**
         * Sets the callback to execute on explicit succession of the request.
         *
         * @param callback the callback to execute
         * @return this builder for chaining
         * @deprecated recommended to use callback providing generic type of {@link Result}
         */
        @Deprecated
        public ServerConnectRequestBuilder callback(final Callback<Boolean> callback)
        {
            this.callback = new Callback<Result>()
            {
                @Override
                public void done(Result result, Throwable error)
                {
                    callback.done( ( result == Result.SUCCESS ) ? Boolean.TRUE : Boolean.FALSE, error );
                }
            };
            return this;
        }
    }
}
