package net.md_5.bungee.protocol.login;

import net.md_5.bungee.protocol.ping.*;
import lombok.Getter;
import net.md_5.bungee.protocol.Protocol;

public class LoginProtocol extends Protocol
{

    @Getter
    private static final LoginProtocol instance = new LoginProtocol();

    private LoginProtocol()
    {
        super( 2 );
    }
}
