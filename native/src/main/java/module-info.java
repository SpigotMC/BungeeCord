module org.spigotmc.bungee.ntive {

    requires com.google.common;
    requires io.netty.buffer;
    requires io.netty.common;
    requires io.netty.transport;
    requires static lombok;

    exports net.md_5.bungee.jni;
    exports net.md_5.bungee.jni.cipher;
}
