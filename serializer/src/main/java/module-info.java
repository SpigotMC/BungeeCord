module org.spigotmc.bungee.serializer {

    requires com.google.common;
    requires com.google.gson;
    requires java.desktop;
    requires org.spigotmc.bungee.chat;
    requires org.spigotmc.bungee.dialog;

    requires static lombok;
    requires static org.jetbrains.annotations;
    
    exports net.md_5.bungee.serializer.chat;
    exports net.md_5.bungee.serializer.dialog;
}
