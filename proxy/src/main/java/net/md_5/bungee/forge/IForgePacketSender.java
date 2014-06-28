package net.md_5.bungee.forge;

import net.md_5.bungee.protocol.packet.PluginMessage;

public interface IForgePacketSender
{
    public void send(PluginMessage message);
}
