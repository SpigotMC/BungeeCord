package net.md_5.bungee;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.Packet2Handshake;
import net.md_5.bungee.packet.Packet3Chat;
import net.md_5.bungee.packet.PacketInputStream;

public class UserConnection extends GenericConnection {

    public final Packet2Handshake handshake;
    public Queue<DefinedPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private ServerConnection server;
    private UpstreamBridge upBridge;
    private DownstreamBridge downBridge;

    public UserConnection(Socket socket, PacketInputStream in, OutputStream out, Packet2Handshake handshake) {
        super(socket, in, out);
        this.handshake = handshake;
        username = handshake.username;
    }

    public void connect(InetSocketAddress serverAddr) {
        try {
            ServerConnection newServer = ServerConnection.connect(serverAddr, handshake, true);
            if (server == null) {
                out.write(newServer.loginPacket.getPacket());
                upBridge = new UpstreamBridge();
                upBridge.start();
            }
            if (downBridge != null) {
                downBridge.interrupt();
            }
            downBridge = new DownstreamBridge();
            server = newServer;
            downBridge.start();
        } catch (KickException ex) {
            destory(ex.getMessage());
        } catch (Exception ex) {
            if (server == null) {
                destory("Could not connect to server");
            } else {
                packetQueue.add(new Packet3Chat(ChatColor.YELLOW + "The server you selected is not up at the moment."));
            }
        }
    }

    public void register() {
        BungeeCord.instance.connections.put(username, this);
    }

    private void destory(String reason) {
        if (BungeeCord.instance.isRunning) {
            BungeeCord.instance.connections.remove(username);
        }
        if (upBridge != null) {
            upBridge.interrupt();
        }
        if (downBridge != null) {
            downBridge.interrupt();
        }
        disconnect(reason);
        if (server != null) {
            server.disconnect("Quitting");
        }
    }

    private class UpstreamBridge extends Thread {

        public UpstreamBridge() {
            super("Upstream Bridge - " + username);
        }

        @Override
        public void run() {
            while (!interrupted()) {
                try {
                    byte[] packet = in.readPacket();
                    server.out.write(packet);
                } catch (IOException ex) {
                    destory("Reached end of stream");
                } catch (Exception ex) {
                    destory(Util.exception(ex));
                }
            }
        }
    }

    private class DownstreamBridge extends Thread {

        public DownstreamBridge() {
            super("Downstream Bridge - " + username);
        }

        @Override
        public void run() {
            while (!interrupted()) {
                try {
                    byte[] packet = server.in.readPacket();
                    out.write(packet);
                    out.flush();
                } catch (IOException ex) {
                    destory("Reached end of stream");
                } catch (Exception ex) {
                    destory(Util.exception(ex));
                }
            }
        }
    }
}
