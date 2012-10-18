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
import net.md_5.bungee.packet.Packet10HeldItem;
import net.md_5.bungee.packet.Packet1Login;
import net.md_5.bungee.packet.Packet2Handshake;
import net.md_5.bungee.packet.Packet3Chat;
import net.md_5.bungee.packet.Packet46GameState;
import net.md_5.bungee.packet.Packet9Respawn;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.packet.PacketInputStream;
import net.md_5.bungee.packet.UndefinedPacket;
import net.md_5.bungee.plugin.StreamDirection;
import net.md_5.bungee.plugin.PacketEvent;

public class UserConnection extends GenericConnection implements CommandSender
{

    public final Packet2Handshake handshake;
    public Queue<DefinedPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private ServerConnection server;
    private UpstreamBridge upBridge;
    private DownstreamBridge downBridge;
    // reconnect stuff
    private Packet10HeldItem heldItem;
    private int clientEntityId;
    private int serverEntityId;
    private volatile boolean reconnecting;

    public UserConnection(Socket socket, PacketInputStream in, OutputStream out, Packet2Handshake handshake)
    {
        super(socket, in, out);
        this.handshake = handshake;
        username = handshake.username;
        BungeeCord.instance.connections.put(username, this);
    }

    public void connect(String server)
    {
        InetSocketAddress addr = BungeeCord.instance.config.getServer(server);
        connect(server, addr);
    }

    private void connect(String name, InetSocketAddress serverAddr)
    {
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
                out.write(new Packet46GameState((byte) 2, (byte) 0).getPacket());
                if (heldItem != null)
                {
                    newServer.out.write(heldItem.getPacket());
                }
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
    public void sendMessage(String message)
    {
        packetQueue.add(new Packet3Chat(message));
    }

    @Override
    public String getName()
    {
        return username;
    }
    
    private boolean runEvents(int packetId, byte[] packet, StreamDirection direction) {
    	if(!BungeeCord.instance.pluginManager.packetHasSubscribers(packetId, direction))
    		return false;
    	
    	final PacketEvent event = new PacketEvent(packetId, packet, server.name, username, direction);
    	BungeeCord.instance.threadPool.execute(new Runnable(){
			@Override
			public void run()
			{
				BungeeCord.instance.pluginManager.onReceivePacket(event);
				if(!event.isCancelled())
				{
					packetQueue.add(new UndefinedPacket(event.getPacketId(), event.getPacketData()));
				}
			}
    	});
    	
    	return true;
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
                    } else if (id == 0x10)
                    {
                        heldItem = new Packet10HeldItem(packet);
                    }
                    
                    EntityMap.rewrite(packet, clientEntityId, serverEntityId);
                    
                    if (sendPacket && !server.socket.isClosed())
                    {
                        if(runEvents(id, packet, StreamDirection.UP))
                        {
                        	continue;
                        }
                        
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

                    while (!packetQueue.isEmpty())
                    {
                        DefinedPacket p = packetQueue.poll();
                        if (p != null)
                        {
                            out.write(p.getPacket());
                        }
                    }
                    
                    EntityMap.rewrite(packet, serverEntityId, clientEntityId);
                    
                    if(runEvents(id, packet, StreamDirection.DOWN))
                    {
                    	continue;
                    }
                    
                    out.write(packet);
                }
            } catch (Exception ex)
            {
                destroySelf(Util.exception(ex));
            }
        }
    }
}
