package net.md_5.bungee.forge;

import java.util.Set;

import net.md_5.bungee.protocol.packet.PluginMessage;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;

public class ForgeUtils {

    public static Set<String> readRegisteredChannels(PluginMessage pluginMessage)
    {
        String channels = new String(pluginMessage.getData(),Charsets.UTF_8);
        String[] split = channels.split("\0");
        Set<String> channelSet = ImmutableSet.copyOf(split);
        return channelSet;
    }
}
