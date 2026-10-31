module org.spigotmc.bungee.query {

    requires com.google.common;
    requires java.logging;
    requires io.netty.buffer;
    requires io.netty.transport;
    requires org.spigotmc.bungee.api;
    requires static lombok;

    exports net.md_5.bungee.query;
}
