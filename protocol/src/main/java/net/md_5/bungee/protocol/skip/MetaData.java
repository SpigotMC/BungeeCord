package net.md_5.bungee.protocol.skip;

import io.netty.buffer.ByteBuf;

class MetaData extends Instruction
{

    @Override
    void read(ByteBuf in)
    {
        int x = in.readUnsignedByte();
        while ( x != 127 )
        {
            int type = x >> 5;
            switch ( type )
            {
                case 0:
                    BYTE.read( in );
                    break;
                case 1:
                    SHORT.read( in );
                    break;
                case 2:
                    INT.read( in );
                    break;
                case 3:
                    FLOAT.read( in );
                    break;
                case 4:
                    STRING.read( in );
                    break;
                case 5:
                    ITEM.read( in );
                    break;
                case 6:
                    in.skipBytes( 12 ); //  int, int, int
                    break;
                default:
                    throw new IllegalArgumentException( "Unknown metadata type " + type );
            }
            x = in.readUnsignedByte();
        }
    }
}
