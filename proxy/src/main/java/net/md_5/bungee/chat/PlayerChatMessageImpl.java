package net.md_5.bungee.chat;

import lombok.Data;
import net.md_5.bungee.api.chat.PlayerChatMessage;
import net.md_5.bungee.protocol.DefinedPacket;

@Data
public class PlayerChatMessageImpl implements PlayerChatMessage
{
    private final DefinedPacket packet;
    private final String message;
    private final boolean signed;
}
