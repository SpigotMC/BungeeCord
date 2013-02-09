package net.md_5.bungee;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.crypto.SecretKey;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.packet.DefinedPacket;
import net.md_5.bungee.packet.Packet2Handshake;
import net.md_5.bungee.packet.PacketCDClientStatus;
import net.md_5.bungee.packet.PacketFCEncryptionResponse;
import net.md_5.bungee.packet.PacketFDEncryptionRequest;
import net.md_5.bungee.packet.PacketFEPing;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketHandler;
import net.md_5.bungee.packet.PacketInputStream;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.io.CipherOutputStream;

public class InitialHandler extends PacketHandler implements Runnable, PendingConnection
{

    private final Socket socket;
    @Getter
    private final ListenerInfo listener;
    private PacketInputStream in;
    private OutputStream out;
    private Packet2Handshake handshake;
    private PacketFDEncryptionRequest request;
    private State thisState = State.HANDSHAKE;

    public InitialHandler(Socket socket, ListenerInfo info) throws IOException
    {
        this.socket = socket;
        this.listener = info;
        in = new PacketInputStream(socket.getInputStream());
        out = socket.getOutputStream();
    }

    private enum State
    {

        HANDSHAKE, ENCRYPT, LOGIN, FINISHED;
    }

    @Override
    public void handle(PacketFEPing ping) throws Exception
    {
        socket.setSoTimeout(100);
        boolean newPing = false;
        try
        {
            socket.getInputStream().read();
            newPing = true;
        } catch (IOException ex)
        {
        }

        ServerPing pingevent = new ServerPing(BungeeCord.PROTOCOL_VERSION, BungeeCord.GAME_VERSION,
                listener.getMotd(), ProxyServer.getInstance().getPlayers().size(), listener.getMaxPlayers());

        pingevent = ProxyServer.getInstance().getPluginManager().callEvent(new ProxyPingEvent(this, pingevent)).getResponse();

        String response = (newPing) ? ChatColor.COLOR_CHAR + "1"
                + "\00" + pingevent.getProtocolVersion()
                + "\00" + pingevent.getGameVersion()
                + "\00" + pingevent.getMotd()
                + "\00" + pingevent.getCurrentPlayers()
                + "\00" + pingevent.getMaxPlayers()
                : pingevent.getMotd() + ChatColor.COLOR_CHAR + pingevent.getCurrentPlayers() + ChatColor.COLOR_CHAR + pingevent.getMaxPlayers();
        disconnect(response);
    }

    @Override
    public void handle(Packet2Handshake handshake) throws Exception
    {
        Preconditions.checkState(thisState == State.HANDSHAKE, "Not expecting HANDSHAKE");
        this.handshake = handshake;
        request = EncryptionUtil.encryptRequest();
        out.write(request.getPacket());
        thisState = State.ENCRYPT;
    }

    @Override
    public void handle(PacketFCEncryptionResponse encryptResponse) throws Exception
    {
        Preconditions.checkState(thisState == State.ENCRYPT, "Not expecting ENCRYPT");

        SecretKey shared = EncryptionUtil.getSecret(encryptResponse, request);
        if (!EncryptionUtil.isAuthenticated(handshake.username, request.serverId, shared))
        {
            throw new KickException("Not authenticated with minecraft.net");
        }

        // Check for multiple connections
        ProxiedPlayer old = ProxyServer.getInstance().getPlayer(handshake.username);
        if (old != null)
        {
            old.disconnect("You are already connected to the server");
        }

        // fire login event
        LoginEvent event = new LoginEvent(this);
        if (event.isCancelled())
        {
            disconnect(event.getCancelReason());
        }

        out.write(new PacketFCEncryptionResponse().getPacket());
        in = new PacketInputStream(new CipherInputStream(socket.getInputStream(), EncryptionUtil.getCipher(false, shared)));
        out = new CipherOutputStream(socket.getOutputStream(), EncryptionUtil.getCipher(true, shared));

        thisState = State.LOGIN;
    }

    @Override
    public void handle(PacketCDClientStatus clientStatus) throws Exception
    {
        Preconditions.checkState(thisState == State.LOGIN, "Not expecting LOGIN");

        UserConnection userCon = new UserConnection(socket, this, in, out, handshake);
        String server = ProxyServer.getInstance().getReconnectHandler().getServer(userCon);
        ServerInfo s = BungeeCord.getInstance().config.getServers().get(server);
        userCon.connect(s, true);

        thisState = State.FINISHED;
    }

    @Override
    public void run()
    {
        try
        {
            while (thisState != State.FINISHED)
            {
                byte[] buf = in.readPacket();
                DefinedPacket packet = DefinedPacket.packet(buf);
                packet.handle(this);
            }
        } catch (Exception ex)
        {
            disconnect("[Proxy Error] " + Util.exception(ex));
            ex.printStackTrace();
        }
    }

    @Override
    public void disconnect(String reason)
    {
        thisState = State.FINISHED;
        try
        {
            out.write(new PacketFFKick(reason).getPacket());
        } catch (IOException ioe)
        {
        } finally
        {
            try
            {
                socket.shutdownOutput();
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
