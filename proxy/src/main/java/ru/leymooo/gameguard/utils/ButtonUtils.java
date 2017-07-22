package ru.leymooo.gameguard.utils;

import io.netty.channel.Channel;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import lombok.Getter;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.protocol.packet.extra.ChunkPacket;
import net.md_5.bungee.protocol.packet.extra.MultiBlockChange;
import net.md_5.bungee.protocol.packet.extra.MultiBlockChange.Block;
import net.md_5.bungee.protocol.packet.extra.PlayerPositionAndLook;
import net.md_5.bungee.protocol.packet.extra.SetSlot;
import ru.leymooo.gameguard.GGConnector;
import ru.leymooo.gameguard.Location;
import ru.leymooo.gameguard.schematic.Schematic;

/**
 *
 * @author Leymooo
 */
public class ButtonUtils
{

    @Getter
    private static Schematic schematic;

    public ButtonUtils()
    {
        try
        {
            InputStream in = getClass().getClassLoader().getResourceAsStream( ( "gg-do-not-edit.schematic" ) );
            schematic = Schematic.loadSchematic( in );
        } catch ( IOException ex )
        {
            BungeeCord.getInstance().getLogger().log( Level.WARNING, "Could not load Schematic", ex );
        }
    }

    public static void pasteSchemAndTeleportPlayer(int x, int y, int z, GGConnector connector)
    {
        Channel channel = connector.getChannel();
        HashMap<String, MultiBlockChange> packets = new HashMap<>();
        List<Block> blocks = Schematic.pasteSchematic( x, y, z, schematic );
        HashMap<Location, Block> buttons = new HashMap<>();
        channel.write( new PlayerPositionAndLook( x + 2.5, y + 5, z + 8.5, -90, -18f, 0, false ), channel.voidPromise() );
        SetSlot slot = connector.getSetSlotPacket();
        slot.setItem( -1 );
        for ( int i = 0; i < 7; i++ )
        {
            channel.write( slot, channel.voidPromise() );
            slot.setSlot( slot.getSlot() - 1 );
        }
        for ( Block b : blocks )
        {
            int chunkX = b.getBlockX() >> 4;
            int chunkZ = b.getBlockZ() >> 4;
            String chunkPos = chunkX + ";" + chunkZ;
            if ( !packets.containsKey( chunkPos ) )
            {
                channel.write( new ChunkPacket( chunkX, chunkZ, new byte[ 256 ] ), channel.voidPromise() );
                packets.put( chunkPos, new MultiBlockChange( new ArrayList<>() ) );
            }
            packets.get( chunkPos ).getBlocks().add( b );
            if ( b.getBlockId() == 35 && b.getBlockData() == 14 )
            {
                buttons.put( new Location( b.getBlockX() - 1, b.getBlockY(), b.getBlockZ(), 0, 0, false, 0 ), b );
            }
        }
        List<Block> woolList = new ArrayList<>( buttons.values() );
        Collections.shuffle( woolList );
        woolList.get( ThreadLocalRandom.current().nextInt( woolList.size() ) ).setBlockData( 5 );
        connector.setButtons( buttons );
        for ( MultiBlockChange packet : packets.values() )
        {
            channel.write( packet, channel.voidPromise() );
        }
        channel.flush();
    }
}
