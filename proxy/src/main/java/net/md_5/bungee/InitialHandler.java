package net.md_5.bungee;

import net.md_5.bungee.config.Configuration;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.packet.Packet2Handshake;
import net.md_5.bungee.packet.PacketFCEncryptionResponse;
import net.md_5.bungee.packet.PacketFDEncryptionRequest;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketInputStream;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.io.CipherOutputStream;

public class InitialHandler implements Runnable, PendingConnection
{

    private final Socket socket;
    @Getter
    private final ListenerInfo listener;
    private PacketInputStream in;
    private OutputStream out;
    private Packet2Handshake handshake;

    public InitialHandler(Socket socket, ListenerInfo info) throws IOException
    {
        this.socket = socket;
        this.listener = info;
        in = new PacketInputStream(socket.getInputStream());
        out = socket.getOutputStream();
    }

    @Override
    public void run()
    {
        try
        {
            byte[] packet = in.readPacket();
            int id = Util.getId(packet);
            switch (id)
            {
                case 0x02:
                    handshake = new Packet2Handshake(packet);
                    PacketFDEncryptionRequest request = EncryptionUtil.encryptRequest();
                    out.write(request.getPacket());
                    PacketFCEncryptionResponse response = new PacketFCEncryptionResponse(in.readPacket());

                    SecretKey shared = EncryptionUtil.getSecret(response, request);
                    if (!EncryptionUtil.isAuthenticated(handshake.username, request.serverId, shared))
                    {
                        throw new KickException("Not authenticated with minecraft.net");
                    }

                    // Check for multiple connections
                    ProxiedPlayer old = ProxyServer.getInstance().getPlayer(handshake.username);
                    if (old != null)
                    {
                        old.disconnect("You are already connected to the server");
                    }

                    // fire login event
                    LoginEvent event = new LoginEvent(this);
                    if (event.isCancelled())
                    {
                        throw new KickException(event.getCancelReason());
                    }

                    out.write(new PacketFCEncryptionResponse().getPacket());
                    in = new PacketInputStream(new CipherInputStream(socket.getInputStream(), EncryptionUtil.getCipher(false, shared)));
                    out = new CipherOutputStream(socket.getOutputStream(), EncryptionUtil.getCipher(true, shared));
                    List<byte[]> customPackets = new ArrayList<>();
                    byte[] custom;
                    while (Util.getId((custom = in.readPacket())) != 0xCD)
                    {
                        customPackets.add(custom);
                    }

                    UserConnection userCon = new UserConnection(socket, this, in, out, handshake, customPackets);
                    String server = ProxyServer.getInstance().getReconnectHandler().getServer(userCon);
                    ServerInfo s = BungeeCord.getInstance().config.getServers().get(server);
                    userCon.connect(s);
                    break;
                case 0xFE:
                    socket.setSoTimeout(100);
                    boolean newPing = false;
                    try
                    {
                        socket.getInputStream().read();
                        newPing = true;
                    } catch (IOException ex)
                    {
                    }

                    ServerPing pingevent = new ServerPing(BungeeCord.PROTOCOL_VERSION, BungeeCord.GAME_VERSION,
                            listener.getMotd(), ProxyServer.getInstance().getPlayers().size(), listener.getMaxPlayers());

                    ProxyServer.getInstance().getPluginManager().callEvent(new ProxyPingEvent(this, pingevent));

                    String ping = (newPing) ? ChatColor.COLOR_CHAR + "1"
                            + "\00" + pingevent.getProtocolVersion()
                            + "\00" + pingevent.getGameVersion()
                            + "\00" + pingevent.getMotd()
                            + "\00" + pingevent.getCurrentPlayers()
                            + "\00" + pingevent.getMaxPlayers()
                            : pingevent.getMotd() + ChatColor.COLOR_CHAR + pingevent.getCurrentPlayers() + ChatColor.COLOR_CHAR + pingevent.getMaxPlayers();
                    throw new KickException(ping);
                default:
                    if (id == 0xFA)
                    {
                        run(); // WTF Spoutcraft
                    } else
                    {
                        // throw new IllegalArgumentException("Wasn't ready for packet id " + Util.hex(id));
                    }
            }
        } catch (KickException ex)
        {
            disconnect(ex.getMessage());
        } catch (Exception ex)
        {
            disconnect("[Proxy Error] " + Util.exception(ex));
            ex.printStackTrace();
        }
    }

    @Override
    public void disconnect(String reason)
    {
        try
        {
            out.write(new PacketFFKick(reason).getPacket());
        } catch (IOException ioe)
        {
        } finally
        {
            try
            {
                out.flush();
                socket.close();
            } catch (IOException ioe2)
            {
            }
        }
    }

    @Override
    public String getName()
    {
        return (handshake == null) ? null : handshake.username;
    }

    @Override
    public byte getVersion()
    {
        return (handshake == null) ? -1 : handshake.procolVersion;
    }

    @Override
    public InetSocketAddress getVirtualHost()
    {
        return (handshake == null) ? null : new InetSocketAddress(handshake.host, handshake.port);
    }

    @Override
    public InetSocketAddress getAddress()
    {
        return (InetSocketAddress) socket.getRemoteSocketAddress();
    }
}
