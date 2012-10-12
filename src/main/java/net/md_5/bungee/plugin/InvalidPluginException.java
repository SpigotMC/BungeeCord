package net.md_5.bungee.plugin;

/**
 * Exception thrown when a plugin could not be loaded for any reason.
 */
public class InvalidPluginException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPluginException(String message) {
        super(message);
    }
}
