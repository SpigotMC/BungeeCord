package net.md_5.bungee;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.crypto.SecretKey;
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
public class ServerConnection extends GenericConnection
{

    public final String name;
    public final Packet1Login loginPacket;
    public Queue<DefinedPacket> packetQueue = new ConcurrentLinkedQueue<>();

    public ServerConnection(String name, Socket socket, PacketInputStream in, OutputStream out, Packet1Login loginPacket)
    {
        super(socket, in, out);
        this.name = name;
        this.loginPacket = loginPacket;
    }

    public static ServerConnection connect(UserConnection user, String name, InetSocketAddress address, Packet2Handshake handshake, boolean retry)
    {
        try
        {
            Socket socket = new Socket();
            socket.connect(address, BungeeCord.instance.config.timeout);
            BungeeCord.instance.setSocketOptions(socket);

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

            // Register all global plugin message channels
            // TODO: Allow player-specific plugin message channels for full mod support
            for (String channel : BungeeCord.instance.globalPluginChannels)
            {
                out.write(new PacketFAPluginMessage("REGISTER", channel.getBytes()).getPacket());
            }

            return new ServerConnection(name, socket, in, out, login);
        } catch (KickException ex)
        {
            throw ex;
        } catch (Exception ex)
        {
            InetSocketAddress def = BungeeCord.instance.config.getServer(null);
            if (retry && !address.equals(def))
            {
                return connect(user, name, def, handshake, false);
            } else
            {
                throw new RuntimeException("Could not connect to target server " + Util.exception(ex));
            }
        }
    }
}
