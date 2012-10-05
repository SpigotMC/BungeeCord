package net.md_5.bungee.plugin;

public class InvalidPluginException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPluginException(String message) {
        super(message);
    }
}
