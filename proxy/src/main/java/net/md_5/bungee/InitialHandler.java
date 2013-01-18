package net.md_5.bungee;

import net.md_5.bungee.config.Configuration;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.SecretKey;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
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
    private PacketInputStream in;
    private OutputStream out;
    private Packet2Handshake handshake;

    public InitialHandler(Socket socket) throws IOException
    {
        this.socket = socket;
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

                    UserConnection userCon = new UserConnection(socket, in, out, handshake, customPackets);
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
                    Configuration conf = BungeeCord.getInstance().config;
                    String ping = (newPing) ? ChatColor.COLOR_CHAR + "1"
                            + "\00" + BungeeCord.PROTOCOL_VERSION
                            + "\00" + BungeeCord.GAME_VERSION
                            + "\00" + conf.motd
                            + "\00" + ProxyServer.getInstance().getPlayers().size()
                            + "\00" + conf.maxPlayers
                            : conf.motd + ChatColor.COLOR_CHAR + ProxyServer.getInstance().getPlayers().size() + ChatColor.COLOR_CHAR + conf.maxPlayers;
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
