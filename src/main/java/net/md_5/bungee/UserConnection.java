package net.md_5.bungee;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.md_5.bungee.command.CommandSender;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.Packet0KeepAlive;
import net.md_5.bungee.packet.Packet1Login;
import net.md_5.bungee.packet.Packet2Handshake;
import net.md_5.bungee.packet.Packet3Chat;
import net.md_5.bungee.packet.Packet9Respawn;
import net.md_5.bungee.packet.PacketC9PlayerListItem;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.packet.PacketInputStream;

public class UserConnection extends GenericConnection implements CommandSender
{    
    public final Packet2Handshake handshake;
    public Queue<DefinedPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private ServerConnection server;
    private UpstreamBridge upBridge;
    private DownstreamBridge downBridge;
    // reconnect stuff
    private int clientEntityId;
    private int serverEntityId;
    private volatile boolean reconnecting;
    // ping stuff
    private int trackingPingId;
    private long pingTime;
    private int ping;

    public UserConnection(Socket socket, PacketInputStream in, OutputStream out, Packet2Handshake handshake)
    {
        super(socket, in, out);
        this.handshake = handshake;
        username = handshake.username;
        BungeeCord.instance.connections.put(username, this);
        BungeeCord.instance.tabListHandler.onJoin(this);
    }

    public void connect(String server)
    {
        InetSocketAddress addr = BungeeCord.instance.config.getServer(server);
        connect(server, addr);
    }

    private void connect(String name, InetSocketAddress serverAddr)
    {
        BungeeCord.instance.tabListHandler.onServerChange(this);
        try
        {
            reconnecting = true;

            if (server != null)
            {
                out.write(new Packet9Respawn((byte) 1, (byte) 0, (byte) 0, (short) 256, "DEFAULT").getPacket());
                out.write(new Packet9Respawn((byte) -1, (byte) 0, (byte) 0, (short) 256, "DEFAULT").getPacket());
            }

            ServerConnection newServer = ServerConnection.connect(name, serverAddr, handshake, server == null);
            if (server == null)
            {
                clientEntityId = newServer.loginPacket.entityId;
                serverEntityId = newServer.loginPacket.entityId;
                out.write(newServer.loginPacket.getPacket());
                upBridge = new UpstreamBridge();
                upBridge.start();
            } else
            {
                try
                {
                    downBridge.interrupt();
                    downBridge.join();
                } catch (InterruptedException ie)
                {
                }

                server.disconnect("Quitting");

                Packet1Login login = newServer.loginPacket;
                serverEntityId = login.entityId;
                out.write(new Packet9Respawn(login.dimension, login.difficulty, login.gameMode, (short) 256, login.levelType).getPacket());
            }
            reconnecting = false;
            downBridge = new DownstreamBridge();
            server = newServer;
            downBridge.start();
        } catch (KickException ex)
        {
            destroySelf(ex.getMessage());
        } catch (Exception ex)
        {
            destroySelf("Could not connect to server");
        }
    }

    public SocketAddress getAddress()
    {
        return socket.getRemoteSocketAddress();
    }
    
    public int getPing()
    {
        return ping;
    }
    
    private void setPing(int ping)
    {
        BungeeCord.instance.tabListHandler.onPingChange(this, ping);
        this.ping = ping;
    }

    private void destroySelf(String reason)
    {
        if (BungeeCord.instance.isRunning)
        {
            BungeeCord.instance.connections.remove(username);
        }
        disconnect(reason);
        if (server != null)
        {
            server.disconnect("Quitting");
            BungeeCord.instance.config.setServer(this, server.name);
        }
    }
    
    @Override
    public void disconnect(String reason) {
        BungeeCord.instance.tabListHandler.onDisconnect(this);
        super.disconnect(reason);
    }

    @Override
    public void sendMessage(String message)
    {
        packetQueue.add(new Packet3Chat(message));
    }

    @Override
    public String getName()
    {
        return username;
    }

    private class UpstreamBridge extends Thread
    {

        public UpstreamBridge()
        {
            super("Upstream Bridge - " + username);
        }

        @Override
        public void run()
        {
            while (!socket.isClosed())
            {
                try
                {
                    byte[] packet = in.readPacket();
                    boolean sendPacket = true;
                    
                    int id = Util.getId(packet);
                    if (id == 0x03)
                    {
                        Packet3Chat chat = new Packet3Chat(packet);
                        String message = chat.message;
                        if (message.startsWith("/"))
                        {
                            sendPacket = !BungeeCord.instance.dispatchCommand(message.substring(1), UserConnection.this);
                        }
                    }
                    else if(id == 0x00)
                    {
                        if(trackingPingId == new Packet0KeepAlive(packet).id)
                        {
                            setPing((int) (System.currentTimeMillis()-pingTime));
                        }
                    }

                    EntityMap.rewrite(packet, clientEntityId, serverEntityId);
                    if (sendPacket && !server.socket.isClosed())
                    {
                        server.out.write(packet);
                    }
                } catch (IOException ex)
                {
                    destroySelf("Reached end of stream");
                } catch (Exception ex)
                {
                    destroySelf(Util.exception(ex));
                }
            }
        }
    }

    private class DownstreamBridge extends Thread
    {

        public DownstreamBridge()
        {
            super("Downstream Bridge - " + username);
        }

        @Override
        public void run()
        {
            try
            {
                while (!reconnecting)
                {
                    byte[] packet = server.in.readPacket();

                    int id = Util.getId(packet);
                    if (id == 0xFA)
                    {
                        PacketFAPluginMessage message = new PacketFAPluginMessage(packet);
                        if (message.tag.equals("RubberBand"))
                        {
                            String server = new String(message.data);
                            connect(server);
                            break;
                        }
                    }
                    else if(id == 0x00)
                    {
                        trackingPingId = new Packet0KeepAlive(packet).id;
                        pingTime = System.currentTimeMillis();
                    }
                    else if(id == 0xC9)
                    {
                        if(!BungeeCord.instance.tabListHandler.onPacketC9(UserConnection.this, new PacketC9PlayerListItem(packet)))
                        {    
                            continue;
                        }
                    }

                    while (!packetQueue.isEmpty())
                    {
                        DefinedPacket p = packetQueue.poll();
                        if (p != null)
                        {
                            out.write(p.getPacket());
                        }
                    }

                    EntityMap.rewrite(packet, serverEntityId, clientEntityId);
                    out.write(packet);
                }
            } catch (Exception ex)
            {
                destroySelf(Util.exception(ex));
            }
        }
    }
}
