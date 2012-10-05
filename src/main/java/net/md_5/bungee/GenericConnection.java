package net.md_5.bungee;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketInputStream;

@EqualsAndHashCode
@RequiredArgsConstructor
public class GenericConnection {

    private final Socket socket;
    private final PacketInputStream in;
    private final OutputStream out;

    public void disconnect(String reason) {
        try {
            out.write(new PacketFFKick(reason).getPacket());
        } catch (IOException ex) {
        } finally {
            try {
                out.flush();
                socket.close();
            } catch (IOException ioe) {
            }
        }
    }
}
