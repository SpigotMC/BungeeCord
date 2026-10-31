module org.spigotmc.bungee.api {

    requires com.google.common;
    requires com.google.gson;
    requires java.desktop;
    requires java.logging;
    requires io.netty.transport.unix.common;
    requires org.objectweb.asm.commons;
    requires org.spigotmc.bungee.chat;
    requires org.spigotmc.bungee.config;
    requires org.spigotmc.bungee.dialog;
    requires org.spigotmc.bungee.event;
    requires org.spigotmc.bungee.protocol;
    requires org.yaml.snakeyaml;
    requires static lombok;
    requires static org.jetbrains.annotations;

    // Library resolver
    requires org.spigotmc.bungee.maven.resolver;
    requires org.apache.maven.resolver;

    exports net.md_5.bungee.api;
    exports net.md_5.bungee.api.config;
    exports net.md_5.bungee.api.connection;
    exports net.md_5.bungee.api.event;
    exports net.md_5.bungee.api.plugin;
    exports net.md_5.bungee.api.scheduler;
    exports net.md_5.bungee.api.score;

    exports net.md_5.bungee.command; // TODO: Proper export/API
}
