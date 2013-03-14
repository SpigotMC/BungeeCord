package net.md_5.bungee;

/**
 * Class to rewrite integers within packets.
 */
public class EntityMap
{

    public final static int[][] entityIds = new int[ 256 ][];

    static
    {
        entityIds[0x05] = new int[]
        {
            1
        };
        entityIds[0x07] = new int[]
        {
            1, 5
        };
        entityIds[0x11] = new int[]
        {
            1
        };
        entityIds[0x12] = new int[]
        {
            1
        };
        entityIds[0x13] = new int[]
        {
            1
        };
        entityIds[0x14] = new int[]
        {
            1
        };
        entityIds[0x16] = new int[]
        {
            1, 5
        };
        entityIds[0x17] = new int[]
        {
            1 //, 20
        };
        entityIds[0x18] = new int[]
        {
            1
        };
        entityIds[0x19] = new int[]
        {
            1
        };
        entityIds[0x1A] = new int[]
        {
            1
        };
        entityIds[0x1C] = new int[]
        {
            1
        };
        entityIds[0x1E] = new int[]
        {
            1
        };
        entityIds[0x1F] = new int[]
        {
            1
        };
        entityIds[0x20] = new int[]
        {
            1
        };
        entityIds[0x21] = new int[]
        {
            1
        };
        entityIds[0x22] = new int[]
        {
            1
        };
        entityIds[0x23] = new int[]
        {
            1
        };
        entityIds[0x26] = new int[]
        {
            1
        };
        entityIds[0x27] = new int[]
        {
            1, 5
        };
        entityIds[0x28] = new int[]
        {
            1
        };
        entityIds[0x29] = new int[]
        {
            1
        };
        entityIds[0x2A] = new int[]
        {
            1
        };
        entityIds[0x37] = new int[]
        {
            1
        };

        entityIds[0x47] = new int[]
        {
            1
        };
    }

    public static void rewrite(byte[] packet, int oldId, int newId)
    {
        int packetId = packet[0] & 0xFF;
        if ( packetId == 0x1D )
        { // bulk entity
            for ( int pos = 2; pos < packet.length; pos += 4 )
            {
                int readId = readInt( packet, pos );
                if ( readId == oldId )
                {
                    setInt( packet, pos, newId );
                } else if ( readId == newId )
                {
                    setInt( packet, pos, oldId );
                }
            }
        } else
        {
            int[] idArray = entityIds[packetId];
            if ( idArray != null )
            {
                for ( int pos : idArray )
                {
                    int readId = readInt( packet, pos );
                    if ( readId == oldId )
                    {
                        setInt( packet, pos, newId );
                    } else if ( readId == newId )
                    {
                        setInt( packet, pos, oldId );
                    }
                }
            }
        }
    }

    private static void setInt(byte[] buf, int pos, int i)
    {
        buf[pos] = (byte) ( i >> 24 );
        buf[pos + 1] = (byte) ( i >> 16 );
        buf[pos + 2] = (byte) ( i >> 8 );
        buf[pos + 3] = (byte) i;
    }

    private static int readInt(byte[] buf, int pos)
    {
        return ( ( ( buf[pos] & 0xFF ) << 24 ) | ( ( buf[pos + 1] & 0xFF ) << 16 ) | ( ( buf[pos + 2] & 0xFF ) << 8 ) | buf[pos + 3] & 0xFF );
    }
}
