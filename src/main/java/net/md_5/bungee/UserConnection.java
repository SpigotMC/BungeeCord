package net.md_5.bungee;

import java.io.OutputStream;
import java.net.Socket;
import net.md_5.bungee.packet.Packet2Handshake;
import net.md_5.bungee.packet.PacketInputStream;

public class UserConnection extends GenericConnection {

    private final Packet2Handshake handshake;

    public UserConnection(Socket socket, PacketInputStream in, OutputStream out, Packet2Handshake handshake) {
        super(socket, in, out);
        this.handshake = handshake;
    }
}
