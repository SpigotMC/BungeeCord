module org.spigotmc.bungee.dialog {

    requires com.google.common;
    requires com.google.gson;
    requires org.spigotmc.bungee.chat;

    requires static lombok;
    requires static org.jetbrains.annotations;

    exports net.md_5.bungee.api.dialog;
    exports net.md_5.bungee.api.dialog.action;
    exports net.md_5.bungee.api.dialog.body;
    exports net.md_5.bungee.api.dialog.chat;
    exports net.md_5.bungee.api.dialog.input;

    opens net.md_5.bungee.api.dialog to com.google.gson;
    opens net.md_5.bungee.api.dialog.action to com.google.gson;
    opens net.md_5.bungee.api.dialog.body to com.google.gson;
    opens net.md_5.bungee.api.dialog.input to com.google.gson;
}
