package ru.leymooo.bungee.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
import ru.leymooo.config.CaptchaConfig;
import ru.leymooo.ycore.Connection;

import com.google.common.base.Preconditions;

public class CaptchaBridge extends PacketHandler {
    //Captcha
    private final UserConnection con;
    private String captcha = null;
    private int captchaId = 0;
    private boolean settings = false;
    private final long joinTime = System.currentTimeMillis();
    private int retrys = 3;
    private boolean tpconfirm = false;
    private boolean mcbrand = false;
    private boolean alive = false;
    private boolean transaction = false;
    private int aliveid;
    //Captcha settings
    private static final Random random = new Random();
    private static int teleportId;
    public static ArrayList<String> strings = new ArrayList<String>();
    private static String TIMEOUT;
    private static String ENTER_CAPTCHA;
    private static String INVALID;
    private static Map<UserConnection, CaptchaBridge> connections = new ConcurrentHashMap<UserConnection, CaptchaBridge>();
    private static CaptchaConfig sql;
    public static AtomicInteger a = new AtomicInteger();

    public static void init() {
        CaptchaBridge.sql = new CaptchaConfig();
        CaptchaBridge.TIMEOUT = CaptchaConfig.messageTimeout;
        CaptchaBridge.ENTER_CAPTCHA = CaptchaConfig.messageEnter;
        CaptchaBridge.INVALID = CaptchaConfig.messageInvalid;
        teleportId = Connection.getTeleportId();
        try {
            CaptchaGenerator captchagenerator = new CaptchaGenerator();
            captchagenerator.generate(CaptchaBridge.sql.threads, CaptchaConfig.maxCaptcha);
        } catch (Exception exception) {
            System.out.println("Exception while generate maps");
            exception.printStackTrace();
            System.exit(0);
        }

    }
    public CaptchaBridge() {
        this.con = null;
    }

    public static int getOnline() {
        return CaptchaBridge.connections.size();
    }

    public UserConnection getCon() {
        return this.con;
    }

    public long getJoinTime() {
        return this.joinTime;
    }

    public CaptchaBridge(UserConnection connection) {
        this.con = connection;
        CaptchaBridge.a.incrementAndGet();
        this.connected();
    }

    private void connected() {
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

        this.write(channel, Connection.login, protocol, id1);
        this.write(channel, Connection.spawnPosition, protocol, id2);
        if (protocol > 47) {
            this.write(channel, Connection.abilities1_9, protocol, 43);
            this.write(channel, Connection.playerPosition, protocol, id3);
            this.write(channel, Connection.chunk1_9, protocol, 32);
            this.write(channel, Connection.setSlot, protocol, 22);
        } else {
            tpconfirm = true;
            this.write(channel, Connection.playerPosition, protocol, id3);
            this.write(channel, Connection.setSlot, protocol, 47);
        }
        aliveid = random.nextInt(999);
        if (protocol > 47) {
            this.write(channel, Connection.transaction, protocol, 17);
            this.write(channel, new KeepAlive(aliveid), protocol, 31);
        } else {
            this.write(channel, Connection.transaction, protocol, 50);
            this.write(channel, new KeepAlive(aliveid), protocol, 0);
        }
        this.resetCaptcha();
        BungeeCord.getInstance().getLogger().info("[" + this.con.getName() + "] <-> CaptchaBridge has connected");
        CaptchaBridge.connections.put(this.con, this);
    }

    private void write(Channel channel, DefinedPacket packet, int protocol, int id) {
        ByteBuf buf = channel.alloc().buffer();

        DefinedPacket.writeVarInt(id, buf);
        packet.write(buf, ProtocolConstants.Direction.TO_CLIENT, protocol);
        channel.write(buf);
    }

    private void write(Channel channel, ByteBuf buf) {
        ByteBuf buffer = buf.copy();

        channel.write(buffer);
    }

    public void exception(Throwable t) throws Exception {
        this.con.disconnect(Util.exception(t));
        CaptchaBridge.connections.remove(this.con);
        BungeeCord.getInstance().removeConnection( this.con );

    }

    public void disconnected(ChannelWrapper channel) throws Exception {
        CaptchaBridge.connections.remove(this.con);
        BungeeCord.getInstance().getPluginManager().callEvent(new PlayerDisconnectEvent(this.con));
        BungeeCord.getInstance().removeConnection( this.con );
    }

    private void resetCaptcha() {
        int protocol = this.con.getPendingConnection().getHandshake().getProtocolVersion();
        Channel channel = this.con.getCh().getHandle();

        this.captchaId = getRandomCaptcha();
        this.captcha = strings.get(captchaId);
        this.write(channel, Connection.getCaptcha(this.captchaId, protocol));
        channel.flush();
    }

    private static int getRandomCaptcha() {
        return random.nextInt(strings.size());
    }
    public void handle(ConfirmTransaction transaction) {
        System.out.print("1");
        this.transaction = true;
    }
    public void handle(PluginMessage message) {
        System.out.print("2");
        if (message.getTag().equals("MC|Brand")) {
            mcbrand = true;
        }
    }
    public void handle(TeleportConfirm confirm) throws Exception {
        System.out.print("3");
        if (confirm.getTeleportId() == teleportId) {
            tpconfirm = true;
        } else {
            this.con.disconnect("§cСкорее всего вы бот :(");
        }
    }
    public void handle(KeepAlive alive) throws Exception {
        System.out.print("4");
        if (alive.getRandomId() == aliveid ) {
            this.alive = true;
        }
    }
    public void handle(ClientSettings settings) throws Exception {
        System.out.print("5");
        this.settings = true;
        this.con.setSettings(settings);
    }
    public void handle(Chat chat) throws Exception {
        Preconditions.checkArgument(chat.getMessage().length() <= 100, "Chat message too long");
        if (!chat.getMessage().equalsIgnoreCase(String.valueOf(this.captcha))) {
            if (--this.retrys == 0) {
                this.con.disconnect("[§cCaptcha§f] Неверная капча");
            } else {
                this.resetCaptcha();
                this.con.sendMessage(String.format(CaptchaBridge.INVALID, new Object[] { Integer.valueOf(this.retrys), this.retrys == 1 ? "а" : "и"}));
            }
        } else {
            this.finish();
        }
    }

    @SuppressWarnings("deprecation")
    private void finish() {
        this.con.serverr = true;
        ((HandlerBoss) this.con.getCh().getHandle().pipeline().get(HandlerBoss.class)).setHandler(new UpstreamBridge(ProxyServer.getInstance(), this.con));
        ProxyServer.getInstance().getPluginManager().callEvent(new PostLoginEvent(this.con));
        this.con.connect(ProxyServer.getInstance().getServerInfo(this.con.getPendingConnection().getListener().getDefaultServer()), (Callback<Boolean>) null, true);
        CaptchaBridge.sql.addIp(this.con.getAddress().getAddress().getHostAddress());
        CaptchaBridge.connections.remove(this.con);
    }


    public String toString() {
        return "[" + this.con.getName() + "] <-> CaptchaBridge ";
    }

    static {
        (new Thread(new Runnable() {
            public void run() {
                while (true) {
                    if (!Thread.interrupted()) {
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException interruptedexception) {
                            interruptedexception.printStackTrace();
                        }

                        long curr = System.currentTimeMillis();
                        Iterator<CaptchaBridge> iterator = CaptchaBridge.connections.values().iterator();
                        CaptchaBridge b;
                        while (iterator.hasNext()) {
                            b = (CaptchaBridge) iterator.next();
                            b.getCon().sendMessage(CaptchaBridge.ENTER_CAPTCHA);
                            if (curr - b.getJoinTime() >= (long) CaptchaConfig.timeout) {
                                b.getCon().disconnect(CaptchaBridge.TIMEOUT);
                                iterator.remove();
                            } else {
                                if (curr - b.getJoinTime() >= 1500) {
                                    if (b.settings == false || b.tpconfirm == false || b.mcbrand == false || b.alive == false || b.transaction == false) {
                                        b.getCon().disconnect("§cСкорее всего вы бот :(");
                                        iterator.remove();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }, "Captcha TimeoutHandler")).start();
    }
}
