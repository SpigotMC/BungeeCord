package net.md_5.bungee.protocol.game;

import lombok.Getter;
import net.md_5.bungee.protocol.Protocol;

public class GameProtocol extends Protocol
{

    @Getter
    private static final GameProtocol instance = new GameProtocol();

    private GameProtocol()
    {
        super( 0 );
    }
}
