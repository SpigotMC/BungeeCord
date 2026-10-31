module org.spigotmc.bungee.chat {

    requires com.google.gson;
    requires com.google.common;
    requires java.desktop;
    requires static lombok;
    requires static org.jetbrains.annotations;

    exports net.md_5.bungee.api.chat;
    exports net.md_5.bungee.api.chat.hover.content;
    exports net.md_5.bungee.api.chat.objects;
    exports net.md_5.bungee.api.chat.player;

    exports net.md_5.bungee.chat; // TODO: Proper export/API
    
    opens net.md_5.bungee.api.chat to com.google.gson;
    opens net.md_5.bungee.api.chat.hover.content to com.google.gson;
}
