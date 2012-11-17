package net.md_5.bungee;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import net.md_5.bungee.packet.Packet2Handshake;
import net.md_5.bungee.packet.PacketFCEncryptionResponse;
import net.md_5.bungee.packet.PacketFDEncryptionRequest;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketInputStream;
import net.md_5.bungee.plugin.LoginEvent;

public class InitialHandler implements Runnable
{

    private final Socket socket;
    private PacketInputStream in;
    private OutputStream out;

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
                    Packet2Handshake handshake = new Packet2Handshake(packet);
                    // fire connect event
                    LoginEvent event = new LoginEvent(handshake.username, socket.getInetAddress(), handshake.host);
                    BungeeCord.instance.pluginManager.onHandshake(event);
                    if (event.isCancelled())
                    {
                        throw new KickException(event.getCancelReason());
                    }

                    PacketFDEncryptionRequest request = EncryptionUtil.encryptRequest();
                    out.write(request.getPacket());
                    PacketFCEncryptionResponse response = new PacketFCEncryptionResponse(in.readPacket());

                    SecretKey shared = EncryptionUtil.getSecret(response, request);
                    if (!EncryptionUtil.isAuthenticated(handshake.username, request.serverId, shared))
                    {
                        throw new KickException("Not authenticated with minecraft.net");
                    }

                    // fire post auth event
                    BungeeCord.instance.pluginManager.onLogin(event);
                    if (event.isCancelled())
                    {
                        throw new KickException(event.getCancelReason());
                    }

                    out.write(new PacketFCEncryptionResponse().getPacket());
                    in = new PacketInputStream(new CipherInputStream(socket.getInputStream(), EncryptionUtil.getCipher(Cipher.DECRYPT_MODE, shared)));
                    out = new CipherOutputStream(socket.getOutputStream(), EncryptionUtil.getCipher(Cipher.ENCRYPT_MODE, shared));
                    List<byte[]> customPackets = new ArrayList<>();
                    byte[] custom;
                    while (Util.getId((custom = in.readPacket())) != 0xCD)
                    {
                        customPackets.add(custom);
                    }

                    UserConnection userCon = new UserConnection(socket, in, out, handshake, customPackets);
                    userCon.connect(BungeeCord.instance.config.getServer(handshake.username, handshake.host));
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
                    Configuration conf = BungeeCord.instance.config;
                    String ping = (newPing) ? ChatColor.COLOR_CHAR + "1"
                            + "\00" + BungeeCord.PROTOCOL_VERSION
                            + "\00" + BungeeCord.GAME_VERSION
                            + "\00" + conf.motd
                            + "\00" + BungeeCord.instance.connections.size()
                            + "\00" + conf.maxPlayers
                            : conf.motd + ChatColor.COLOR_CHAR + BungeeCord.instance.connections.size() + ChatColor.COLOR_CHAR + conf.maxPlayers;
                    throw new KickException(ping);
                default:
                    throw new IllegalArgumentException("Wasn't ready for packet id " + Util.hex(id));
            }
        } catch (KickException ex)
        {
            kick(ex.getMessage());
        } catch (Exception ex)
        {
            kick("[Proxy Error] " + Util.exception(ex));
        }
    }

    private void kick(String message)
    {
        try
        {
            out.write(new PacketFFKick(message).getPacket());
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
}
