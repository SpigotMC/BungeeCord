package net.md_5.bungee.protocol;

import net.md_5.bungee.chat.ChatVersion;
import net.md_5.bungee.chat.VersionedComponentSerializer;

public class ChatSerializer
{

    public static VersionedComponentSerializer forVersion(int protocolVersion)
    {
        if ( protocolVersion >= ProtocolConstants.MINECRAFT_1_21_5 )
        {
            return VersionedComponentSerializer.forVersion( ChatVersion.V1_21_5 );
        } else
        {
            return VersionedComponentSerializer.forVersion( ChatVersion.V1_16 );
        }
    }
}
