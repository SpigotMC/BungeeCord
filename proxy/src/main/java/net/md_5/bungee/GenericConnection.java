package net.md_5.bungee;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import static net.md_5.bungee.Logger.$;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketInputStream;

/**
 * Class to represent a Minecraft connection.
 */
@EqualsAndHashCode
@RequiredArgsConstructor
public class GenericConnection
{

    protected final Socket socket;
    protected final PacketInputStream in;
    protected final OutputStream out;
    public String username;
    public String tabListName;

    /**
     * Close the socket with the specified reason.
     *
     * @param reason to disconnect
     */
    public void disconnect(String reason)
    {
        if (socket.isClosed())
        {
            return;
        }
        log("disconnected with " + reason);
        try
        {
            out.write(new PacketFFKick("[Proxy] " + reason).getPacket());
        } catch (IOException ex)
        {
        } finally
        {
            try
            {
                out.flush();
                out.close();
                socket.close();
            } catch (IOException ioe)
            {
            }
        }
    }

    public void log(String message)
    {
        $().info(socket.getInetAddress() + ((username == null) ? " " : " [" + username + "] ") + message);
    }
}
