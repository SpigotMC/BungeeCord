package net.md_5.bungee.plugin;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class PacketEvent implements Cancellable {
	
	  /**
     * Canceled state.
     */
    private boolean cancelled;
    
    @Setter(AccessLevel.NONE)
    private final int packetId;
    
    private final byte[] packetData;
    
    private final String server;
    
    private final String client;
    
    private final StreamDirection direction;
}
