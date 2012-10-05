package net.md_5.bungee;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import net.md_5.bungee.packet.Packet2Handshake;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketInputStream;

public class InitialHandler implements Runnable {

    private final Socket socket;
    private final PacketInputStream in;
    private final OutputStream out;

    public InitialHandler(Socket socket) throws IOException {
        this.socket = socket;
        in = new PacketInputStream(socket.getInputStream());
        out = socket.getOutputStream();
    }

    @Override
    public void run() {
        try {
            byte[] packet = in.readPacket();
            int id = Util.getId(packet);
            switch (id) {
                case 0x02:
                    Packet2Handshake handshake = new Packet2Handshake(packet);
                    break;
                case 0xFE:
                    throw new KickException(BungeeCord.instance.config.motd + ChatColor.COLOR_CHAR + BungeeCord.instance.getOnlinePlayers() + ChatColor.COLOR_CHAR + BungeeCord.instance.config.maxPlayers);
                default:
                    throw new IllegalArgumentException("Wasn't ready for packet id " + Util.hex(id));
            }
        } catch (KickException ex) {
            kick(ex.getMessage());
        } catch (Exception ex) {
            kick("[Proxy Error] " + Util.exception(ex));
        }
    }

    private void kick(String message) {
        try {
            out.write(new PacketFFKick(message).getPacket());
        } catch (IOException ioe) {
        } finally {
            try {
                socket.close();
            } catch (IOException ioe2) {
            }
        }
    }

    private class KickException extends RuntimeException {

        public KickException(String message) {
            super(message);
        }
    }
}
