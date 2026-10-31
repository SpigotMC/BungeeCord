module org.spigotmc.bungee.protocol {

    requires brigadier;
    requires com.google.common;
    requires com.google.gson;
    requires it.unimi.dsi.fastutil.core;
    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.transport;
    requires org.spigotmc.bungee.chat;
    requires org.spigotmc.bungee.dialog;
    requires org.spigotmc.bungee.nbt;
    requires org.spigotmc.bungee.serializer;
    requires static lombok;

    exports net.md_5.bungee.protocol;
    exports net.md_5.bungee.protocol.channel;
    exports net.md_5.bungee.protocol.data;
    exports net.md_5.bungee.protocol.packet;
    exports net.md_5.bungee.protocol.util;
}
