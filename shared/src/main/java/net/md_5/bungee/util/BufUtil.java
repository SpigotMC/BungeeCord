package net.md_5.bungee.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BufUtil
{
    public String dump(ByteBuf buf, int maxLen)
    {
        return ByteBufUtil.hexDump( buf, 0, Math.min( buf.writerIndex(), maxLen ) );
    }
}
