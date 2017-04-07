package ru.leymooo.bungee.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.connection.UpstreamBridge;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.ClientSettings;
import net.md_5.bungee.protocol.packet.ConfirmTransaction;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.PluginMessage;
import net.md_5.bungee.protocol.packet.TeleportConfirm;
import ru.leymooo.ycore.Connection;

public class CaptchaBridge extends PacketHandler {
    public final UserConnection con;
    public String captcha = null;
    public int captchaId = 0;
    public boolean settings = false;
    public long joinTime = System.currentTimeMillis();
    public int retrys = 3;
    public boolean tpconfirm = false;
    public boolean mcbrand = false;
    public boolean alive = false;
    public boolean transaction = false;
    public int aliveid;
    public String ip;
    public boolean needReset = true;
    
    public static void init() {
        StaticMethods.init();
    }
    public CaptchaBridge() {
        this.con = null;
    }

    public UserConnection getCon() {
        return this.con;
    }

    public long getJoinTime() {
        return this.joinTime;
    }
    public void setJoinTime() {
        this.joinTime = System.currentTimeMillis();
    }
    public CaptchaBridge(UserConnection connection) {
        this.con = connection;
        StaticMethods.a.incrementAndGet();
        this.connected();
    }

    public void connected() {
        ip = this.con.getAddress().getAddress().getHostAddress();
        if (!StaticMethods.underAttack && System.currentTimeMillis() - StaticMethods.start > 1000*60*3 && StaticMethods.a.getAndIncrement() > StaticMethods.sql.maxConnects) {
            StaticMethods.underAttack = true;
            if ((StaticMethods.t!=null && StaticMethods.t.isAlive())==false) { 
                StaticMethods.AttackDetected();
            }
        }
        Channel channel = this.con.getCh().getHandle();
        int protocol = this.con.getPendingConnection().getHandshake().getProtocolVersion();
        this.con.setClientEntityId(-1);
        byte id1;
        byte id2;
        byte id3;
        if (protocol > 47) {
            id1 = 35;
            id2 = 67;
            id3 = 46;
        } else {
            id1 = 1;
            id2 = 5;
            id3 = 8;
        }
        aliveid = StaticMethods.random.nextInt(999);
        this.write(channel, Connection.login, protocol, id1);
        this.write(channel, Connection.spawnPosition, protocol, id2);
        if (protocol > 47) {
            this.write(channel, Connection.abilities1_9, protocol, 43);
            this.write(channel, Connection.playerPosition, protocol, id3);
            this.write(channel, Connection.chunk1_9, protocol, 32);
            this.write(channel, Connection.setSlot, protocol, 22);
            this.write(channel, Connection.transaction, protocol, 17);
            this.write(channel, new KeepAlive(aliveid), protocol, 31);
        } else {
            tpconfirm = true;
            this.write(channel, Connection.playerPosition, protocol, id3);
            this.write(channel, Connection.setSlot, protocol, 47);
            this.write(channel, Connection.transaction, protocol, 50);
            this.write(channel, new KeepAlive(aliveid), protocol, 0);
        }
        if (!StaticMethods.underAttack) {
            needReset = false;
            this.resetCaptcha();
        }
        StaticMethods.title.send(this.con);
        BungeeCord.getInstance().getLogger().info("[" + this.con.getName() + "] <-> CaptchaBridge has connected");
        StaticMethods.connections.put(this.con, this);
    }

    public void write(Channel channel, DefinedPacket packet, int protocol, int id) {
        ByteBuf buf = channel.alloc().buffer();

        DefinedPacket.writeVarInt(id, buf);
        packet.write(buf, ProtocolConstants.Direction.TO_CLIENT, protocol);
        channel.write(buf);
    }

    public void write(Channel channel, ByteBuf buf) {
        ByteBuf buffer = buf.copy();

        channel.write(buffer);
    }

    public void exception(Throwable t) throws Exception {
        this.con.disconnect(Util.exception(t));
        StaticMethods.connections.remove(this.con);
        BungeeCord.getInstance().removeConnection( this.con );

    }

    public void disconnected(ChannelWrapper channel) throws Exception {
        if (StaticMethods.underAttack) StaticMethods.bannedips.add(ip);
        StaticMethods.connections.remove(this.con);
        BungeeCord.getInstance().getPluginManager().callEvent(new PlayerDisconnectEvent(this.con));
        BungeeCord.getInstance().removeConnection( this.con );
    }

    public void resetCaptcha() {
        int protocol = this.con.getPendingConnection().getHandshake().getProtocolVersion();
        Channel channel = this.con.getCh().getHandle();

        this.captchaId = StaticMethods.getRandomCaptcha();
        this.captcha = StaticMethods.underAttack ?StaticMethods.strings_attack.get(captchaId):StaticMethods.strings.get(captchaId);
        this.write(channel, Connection.getCaptcha(this.captchaId, protocol));
        channel.flush();
    }

    public void handle(ConfirmTransaction transaction) {
        this.transaction = true;
    }
    public void handle(PluginMessage message) {
        if (message.getTag().equals("MC|Brand")) {
            mcbrand = true;
        }
    }
    public void handle(TeleportConfirm confirm) throws Exception {
        if (confirm.getTeleportId() == StaticMethods.teleportId) {
            tpconfirm = true;
        } 
    }
    public void handle(KeepAlive alive) throws Exception {
        if (alive.getRandomId() == aliveid ) {
            this.alive = true;
        }
    }
    public void handle(ClientSettings settings) throws Exception {
        this.settings = true;
        this.con.setSettings(settings);
    }
    public void handle(Chat chat) throws Exception {
        String msg = chat.getMessage().toLowerCase();
        if (msg.length() >= 5 || !msg.equalsIgnoreCase(String.valueOf(this.captcha))) {
            if (--this.retrys == 0) {
                if (StaticMethods.underAttack) StaticMethods.bannedips.add(ip);
                this.con.disconnect(StaticMethods.sql.wrongCaptcha);
            } else {
                this.resetCaptcha();
                this.con.sendMessage(String.format(StaticMethods.sql.messageInvalid, new Object[] { Integer.valueOf(this.retrys), this.retrys == 1 ? "а" : "и"}));
            }
        } else {
            this.finish();
        }
    }

    @SuppressWarnings("deprecation")
    public void finish() {
        StaticMethods.titleClear.send(this.con);
        this.con.serverr = true;
        ((HandlerBoss) this.con.getCh().getHandle().pipeline().get(HandlerBoss.class)).setHandler(new UpstreamBridge(ProxyServer.getInstance(), this.con));
        ProxyServer.getInstance().getPluginManager().callEvent(new PostLoginEvent(this.con));
        this.con.connect(ProxyServer.getInstance().getServerInfo(this.con.getPendingConnection().getListener().getDefaultServer()), (Callback<Boolean>) null, true);
        StaticMethods.sql.addIp(this.con.getAddress().getAddress().getHostAddress());
        StaticMethods.connections.remove(this.con);
    }


    public String toString() {
        return "[" + this.con.getName() + "] <-> CaptchaBridge ";
    }
}