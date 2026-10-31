module org.spigotmc.bungee.log {

    requires com.google.common;
    requires java.logging;
    requires org.jline;
    requires org.spigotmc.bungee.chat;
    requires static lombok;

    exports net.md_5.bungee.log;
}
