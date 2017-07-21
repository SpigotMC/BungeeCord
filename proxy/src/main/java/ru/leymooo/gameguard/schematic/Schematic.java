package ru.leymooo.gameguard.schematic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import net.md_5.bungee.protocol.packet.extra.MultiBlockChange.Block;
import org.jnbt.ByteArrayTag;
import org.jnbt.CompoundTag;
import org.jnbt.NBTInputStream;
import org.jnbt.ShortTag;
import org.jnbt.StringTag;
import org.jnbt.Tag;

/*
*
*    This class is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    This class is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with this class.  If not, see <http://www.gnu.org/licenses/>.
*
 */
/**
 *
 * @author Max
 */
public class Schematic
{

    private short[] blocks;
    private byte[] data;
    private short width;
    private short lenght;
    private short height;

    public Schematic(short[] blocks, byte[] data, short width, short lenght, short height)
    {
        this.blocks = blocks;
        this.data = data;
        this.width = width;
        this.lenght = lenght;
        this.height = height;
    }

    /**
     * @return the blocks
     */
    public short[] getBlocks()
    {
        return blocks;
    }

    /**
     * @return the data
     */
    public byte[] getData()
    {
        return data;
    }

    /**
     * @return the width
     */
    public short getWidth()
    {
        return width;
    }

    /**
     * @return the lenght
     */
    public short getLenght()
    {
        return lenght;
    }

    /**
     * @return the height
     */
    public short getHeight()
    {
        return height;
    }

    public static List<Block> pasteSchematic(int startX, int startY, int startZ, Schematic schematic)
    {
        List<Block> blocks1 = new ArrayList<>();
        short[] blocks = schematic.getBlocks();
        byte[] blockData = schematic.getData();

        short length = schematic.getLenght();
        short width = schematic.getWidth();
        short height = schematic.getHeight();

        for ( int x = 0; x < width; ++x )
        {
            for ( int y = 0; y < height; ++y )
            {
                for ( int z = 0; z < length; ++z )
                {
                    int index = y * width * length + z * width + x;
                    Block block = new Block( blocks[index], blockData[index], x + startX, y + startY, z + startZ );
                    if ( block.getBlockId() >= 0 )
                    {
                        blocks1.add( block );
                    }
                }
            }
        }
        return blocks1;
    }

    public static Schematic loadSchematic(InputStream file) throws IOException
    {
        NBTInputStream nbtStream = new NBTInputStream( file );

        CompoundTag schematicTag = (CompoundTag) nbtStream.readTag();
        if ( !schematicTag.getName().equals( "Schematic" ) )
        {
            throw new IllegalArgumentException( "Tag \"Schematic\" does not exist or is not first" );
        }

        Map<String, Tag> schematic = schematicTag.getValue();
        if ( !schematic.containsKey( "Blocks" ) )
        {
            throw new IllegalArgumentException( "Schematic file is missing a \"Blocks\" tag" );
        }

        short width = getChildTag( schematic, "Width", ShortTag.class ).getValue();
        short length = getChildTag( schematic, "Length", ShortTag.class ).getValue();
        short height = getChildTag( schematic, "Height", ShortTag.class ).getValue();

        // Get blocks
        byte[] blockId = getChildTag( schematic, "Blocks", ByteArrayTag.class ).getValue();
        byte[] blockData = getChildTag( schematic, "Data", ByteArrayTag.class ).getValue();
        byte[] addId = new byte[ 0 ];
        short[] blocks = new short[ blockId.length ]; // Have to later combine IDs

        // We support 4096 block IDs using the same method as vanilla Minecraft, where
        // the highest 4 bits are stored in a separate byte array.
        if ( schematic.containsKey( "AddBlocks" ) )
        {
            addId = getChildTag( schematic, "AddBlocks", ByteArrayTag.class ).getValue();
        }

        // Combine the AddBlocks data with the first 8-bit block ID
        for ( int index = 0; index < blockId.length; index++ )
        {
            if ( ( index >> 1 ) >= addId.length )
            { // No corresponding AddBlocks index
                blocks[index] = (short) ( blockId[index] & 0xFF );
            } else
            {
                if ( ( index & 1 ) == 0 )
                {
                    blocks[index] = (short) ( ( ( addId[index >> 1] & 0x0F ) << 8 ) + ( blockId[index] & 0xFF ) );
                } else
                {
                    blocks[index] = (short) ( ( ( addId[index >> 1] & 0xF0 ) << 4 ) + ( blockId[index] & 0xFF ) );
                }
            }
        }

        return new Schematic( blocks, blockData, width, length, height );
    }

    /**
     * Get child tag of a NBT structure.
     *
     * @param items The parent tag map
     * @param key The name of the tag to get
     * @param expected The expected type of the tag
     * @return child tag casted to the expected type
     * @throws DataException if the tag does not exist or the tag is not of the
     * expected type
     */
    private static <T extends Tag> T getChildTag(Map<String, Tag> items, String key, Class<T> expected) throws IllegalArgumentException
    {
        if ( !items.containsKey( key ) )
        {
            throw new IllegalArgumentException( "Schematic file is missing a \"" + key + "\" tag" );
        }
        Tag tag = items.get( key );
        if ( !expected.isInstance( tag ) )
        {
            throw new IllegalArgumentException( key + " tag is not of tag type " + expected.getName() );
        }
        return expected.cast( tag );
    }
}
