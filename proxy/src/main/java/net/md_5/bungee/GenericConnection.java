package net.md_5.bungee;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import static net.md_5.bungee.Logger.$;
import net.md_5.bungee.packet.PacketFFKick;
import net.md_5.bungee.packet.PacketStream;

/**
 * Class to represent a Minecraft connection.
 */
@EqualsAndHashCode
@RequiredArgsConstructor
public class GenericConnection
{

    protected final Socket socket;
    protected final PacketStream stream;
    @Getter
    public String name;
    @Getter
    public String displayName;

    /**
     * Close the socket with the specified reason.
     *
     * @param reason to disconnect
     */
    public void disconnect(String reason)
    {
        if ( socket.isClosed() )
        {
            return;
        }
        log( "disconnected with " + reason );
        try
        {
            stream.write( new PacketFFKick( "[scHub] " + reason ) );
        } catch ( IOException ex )
        {
        } finally
        {
            try
            {
                socket.shutdownOutput();
                socket.close();
            } catch ( IOException ioe )
            {
            }
        }
    }

    public void log(String message)
    {
        $().info( socket.getInetAddress() + ( ( name == null ) ? " " : " [" + name + "] " ) + message );
    }
}
