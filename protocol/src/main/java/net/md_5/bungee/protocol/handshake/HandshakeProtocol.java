package net.md_5.bungee.protocol.handshake;

import lombok.Getter;
import net.md_5.bungee.protocol.Protocol;

public class HandshakeProtocol extends Protocol
{

    @Getter
    private static final HandshakeProtocol instance = new HandshakeProtocol();

    private HandshakeProtocol()
    {
        super( 0xF );
        registerPacket( 0, Packet0Handshake.class );
    }
}
