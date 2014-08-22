package net.md_5.bungee.forge;

import net.md_5.bungee.protocol.packet.PluginMessage;

public interface IForgePluginMessageSender
{
    public void send(PluginMessage message);
}
