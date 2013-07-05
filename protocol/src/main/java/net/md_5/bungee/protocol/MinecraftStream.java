package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MinecraftStream
{

    private final ByteBuf buf;

    public byte readByte()
    {
        return buf.readByte();
    }

    public MinecraftStream writeByte(byte b)
    {
        buf.writeByte( b );
        return this;
    }
    /*========================================================================*/

    public short readUnisgnedByte()
    {
        return buf.readUnsignedByte();
    }

    /*========================================================================*/
    public int readInt()
    {
        return buf.readInt();
    }

    public void writeInt(int i)
    {
        buf.writeInt( i );
    }
    /*========================================================================*/

    public String readString()
    {
        short len = buf.readShort();
        char[] c = new char[ len ];
        for ( int i = 0; i < c.length; i++ )
        {
            c[i] = buf.readChar();
        }

        return new String( c );
    }

    public void writeString(String s)
    {
        char[] cc = s.toCharArray();
        buf.writeShort( cc.length );
        for ( char c : cc )
        {
            buf.writeChar( c );
        }
    }
}
