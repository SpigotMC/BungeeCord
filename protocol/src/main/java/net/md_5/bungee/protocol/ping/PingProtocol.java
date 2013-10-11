package net.md_5.bungee.protocol.ping;

import lombok.Getter;
import net.md_5.bungee.protocol.Protocol;

public class PingProtocol extends Protocol
{

    @Getter
    private static final PingProtocol instance = new PingProtocol();

    private PingProtocol()
    {
        super( 1 );
    }
}
