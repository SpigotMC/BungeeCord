package net.md_5.bungee.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BufUtil
{

    public static String dump(ByteBuf buf, int maxLen)
    {
        return ByteBufUtil.hexDump( buf, 0, Math.min( buf.writerIndex(), maxLen ) );
    }
}
