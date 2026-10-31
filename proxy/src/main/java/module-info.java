module org.spigotmc.bungee.proxy {

    requires com.google.common;
    requires com.google.gson;
    requires io.netty.buffer;
    requires io.netty.codec.haproxy;
    requires io.netty.codec.http;
    requires io.netty.codec;
    requires io.netty.common;
    requires io.netty.handler;
    requires io.netty.transport;
    requires io.netty.transport.classes.epoll;
    requires io.netty.transport.classes.io_uring;
    requires io.netty.transport.unix.common;
    requires java.desktop;
    requires java.logging;
    requires jopt.simple;
    requires org.spigotmc.bungee.api;
    requires org.spigotmc.bungee.chat;
    requires org.spigotmc.bungee.dialog;
    requires org.spigotmc.bungee.log;
    requires org.spigotmc.bungee.nbt;
    requires org.spigotmc.bungee.ntive;
    requires org.spigotmc.bungee.protocol;
    requires org.spigotmc.bungee.query;
    requires org.spigotmc.bungee.serializer;
    requires org.spigotmc.bungee.slf4j;
    requires org.yaml.snakeyaml;
    requires static lombok;

    exports net.md_5.bungee.launcher;
}
