package de.luca.betterbungee;

import de.luca.betterbungee.ipcheck.IPChecker;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import net.md_5.bungee.netty.ChannelWrapper;

import java.net.SocketAddress;

public class BetterBungeeAPI {
    @Getter
    private static IPChecker ipchecker = new IPChecker();

    @Getter
    private static String prefix = "§6BetterBungee";

    @Getter
    private static String BetterBungeeVersion = "1.0";

    @Getter
    private static String bungeecordversion = "d99570214aca18a7eb0f6682a36d8992da65b57a";

    public static String getRealAdress(ChannelHandlerContext ctx) {
        final SocketAddress remote = ctx.channel().remoteAddress();
        final String addr = remote != null ? remote.toString() : "";
        return addr.split("/")[1].split(":")[0];
    }

    public static String getRealAdress(SocketAddress socketaddress) {
        final String addr = socketaddress.toString() != null ? socketaddress.toString() : "";
        return addr.split("/")[1].split(":")[0];
    }

    public static String getRealAdress(ChannelWrapper channel) {
        final SocketAddress remote = channel.getRemoteAddress();
        final String addr = remote != null ? remote.toString() : "";
        return addr.split("/")[1].split(":")[0];
    }
}
