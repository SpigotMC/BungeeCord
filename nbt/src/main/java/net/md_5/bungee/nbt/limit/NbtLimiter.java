package net.md_5.bungee.nbt.limit;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.nbt.exception.NbtLimitException;

@RequiredArgsConstructor
public class NbtLimiter
{
    private static final int MAX_STACK_DEPTH = 512;

    private final long maxBytes;
    private final int maxDepth;

    public static NbtLimiter unlimitedSize()
    {
        return new NbtLimiter( Long.MAX_VALUE, MAX_STACK_DEPTH );
    }

    public NbtLimiter(long maxBytes)
    {
        this( maxBytes, MAX_STACK_DEPTH );
    }

    private long usedBytes;
    private int depth;

    public void countBytes(long amount)
    {
        if ( amount < 0 )
        {
            throw new NbtLimitException( "NBT limiter tried to count negative byte amount" );
        }
        if ( usedBytes > ( usedBytes = Math.addExact( usedBytes, amount ) ) )
        {
            throw new NbtLimitException( "NBT tag is to big, bytes > " + maxBytes );
        }
    }

    public void countBytes(long amount, long factor)
    {
        if ( amount < 0 || factor < 0 )
        {
            throw new NbtLimitException( "NBT limiter tried to count negative byte amount" );
        }
        countBytes( Math.multiplyExact( amount, factor ) );
    }

    public void push()
    {
        if ( ( depth = Math.addExact( depth, 1 ) ) > maxDepth )
        {
            throw new NbtLimitException( "NBT tag is to complex, depth > " + maxDepth );
        }
    }

    public void pop()
    {
        if ( --depth < 0 )
        {
            throw new NbtLimitException( "NBT limiter tried to pop depth 0" );
        }
    }
}
