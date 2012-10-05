package net.md_5.bungee.plugin;

import java.net.InetAddress;
import lombok.Data;

@Data
public class ConnectEvent implements Cancellable {

    private boolean cancelled;
    private String cancelReason;
    private final String username;
    private final InetAddress address;
}
