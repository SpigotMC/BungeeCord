package net.md_5.bungee;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import net.md_5.bungee.packet.Packet1Login;
import net.md_5.bungee.packet.Packet2Handshake;
import net.md_5.bungee.packet.PacketCDClientStatus;
import net.md_5.bungee.packet.PacketFCEncryptionResponse;
import net.md_5.bungee.packet.PacketFDEncryptionRequest;
import net.md_5.bungee.packet.PacketInputStream;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.io.CipherOutputStream;

public class ServerConnection extends GenericConnection {

    private final Packet1Login loginPacket;

    public ServerConnection(Socket socket, PacketInputStream in, OutputStream out, Packet1Login loginPacket) {
        super(socket, in, out);
        this.loginPacket = loginPacket;
    }

    public static ServerConnection connect(InetSocketAddress address, Packet2Handshake handshake) throws Exception {
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

        int ciphId = Util.getId(in.readPacket());
        if (ciphId != 0xFC) {
            throw new RuntimeException("Server did not send encryption enable");
        }

        in = new PacketInputStream(new CipherInputStream(socket.getInputStream(), EncryptionUtil.getCipher(false, myKey)));
        out = new CipherOutputStream(out, EncryptionUtil.getCipher(true, myKey));

        out.write(new PacketCDClientStatus((byte) 0).getPacket());
        Packet1Login login = new Packet1Login(in.readPacket());

        return new ServerConnection(socket, in, out, login);
    }
}
