package net.md_5.bungee;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.crypto.SecretKey;
import lombok.Getter;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.Packet1Login;
import net.md_5.bungee.packet.Packet2Handshake;
import net.md_5.bungee.packet.PacketCDClientStatus;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.packet.PacketFCEncryptionResponse;
import net.md_5.bungee.packet.PacketFDEncryptionRequest;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketInputStream;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.io.CipherOutputStream;

/**
 * Class representing a connection from the proxy to the server; ie upstream.
 */
public class ServerConnection extends GenericConnection implements Server
{

    @Getter
    private final ServerInfo info;
    public final Packet1Login loginPacket;
    public Queue<DefinedPacket> packetQueue = new ConcurrentLinkedQueue<>();

    public ServerConnection(Socket socket, ServerInfo info, PacketInputStream in, OutputStream out, Packet1Login loginPacket)
    {
        super(socket, in, out);
        this.info = info;
        this.loginPacket = loginPacket;
    }

    public static ServerConnection connect(UserConnection user, ServerInfo info, Packet2Handshake handshake, boolean retry)
    {
        try
        {
            Socket socket = new Socket();
            socket.connect(info.getAddress(), BungeeCord.getInstance().config.getTimeout());
            BungeeCord.getInstance().setSocketOptions(socket);

            PacketInputStream in = new PacketInputStream(socket.getInputStream());
            OutputStream out = socket.getOutputStream();

            out.write(handshake.getPacket());
            PacketFDEncryptionRequest encryptRequest = new PacketFDEncryptionRequest(in.readPacket());

            SecretKey myKey = EncryptionUtil.getSecret();
            PublicKey pub = EncryptionUtil.getPubkey(encryptRequest);

            PacketFCEncryptionResponse response = new PacketFCEncryptionResponse(EncryptionUtil.getShared(myKey, pub), EncryptionUtil.encrypt(pub, encryptRequest.verifyToken));
            out.write(response.getPacket());

            int ciphId = Util.getId(in.readPacket());
            if (ciphId != 0xFC)
            {
                throw new RuntimeException("Server did not send encryption enable");
            }

            in = new PacketInputStream(new CipherInputStream(socket.getInputStream(), EncryptionUtil.getCipher(false, myKey)));
            out = new CipherOutputStream(out, EncryptionUtil.getCipher(true, myKey));

            for (byte[] custom : user.loginPackets)
            {
                out.write(custom);
            }

            out.write(new PacketCDClientStatus((byte) 0).getPacket());
            byte[] loginResponse = in.readPacket();
            if (Util.getId(loginResponse) == 0xFF)
            {
                throw new KickException("[Kicked] " + new PacketFFKick(loginResponse).message);
            }
            Packet1Login login = new Packet1Login(loginResponse);

            ServerConnection server = new ServerConnection(socket, info, in, out, login);
            ServerConnectedEvent event = new ServerConnectedEvent(user, server);
            ProxyServer.getInstance().getPluginManager().callEvent(event);

            out.write(BungeeCord.getInstance().registerChannels().getPacket());

            return server;
        } catch (KickException ex)
        {
            throw ex;
        } catch (Exception ex)
        {
            ServerInfo def = ProxyServer.getInstance().getServers().get(user.getPendingConnection().getListener().getDefaultServer());
            if (retry && !info.equals(def))
            {
                user.sendMessage(ChatColor.RED + "Could not connect to target server, you have been moved to the default server");
                return connect(user, def, handshake, false);
            } else
            {
                throw new RuntimeException("Could not connect to target server " + Util.exception(ex));
            }
        }
    }

    @Override
    public void sendData(String channel, byte[] data)
    {
        packetQueue.add(new PacketFAPluginMessage(channel, data));
    }

    @Override
    public void ping(final Callback<ServerPing> callback)
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Socket socket = new Socket();
                    socket.connect(getAddress());
                    try (DataOutputStream out = new DataOutputStream(socket.getOutputStream()))
                    {
                        out.write(0xFE);
                        out.write(0x01);
                    }
                    try (PacketInputStream in = new PacketInputStream(socket.getInputStream()))
                    {
                        PacketFFKick response = new PacketFFKick(in.readPacket());

                        String[] split = response.message.split("\00");

                        ServerPing ping = new ServerPing(Byte.parseByte(split[1]), split[2], split[3], Integer.parseInt(split[4]), Integer.parseInt(split[5]));
                        callback.done(ping, null);
                    }
                } catch (Throwable t)
                {
                    callback.done(null, t);
                }
            }
        }.start();
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return getInfo().getAddress();
    }
}
