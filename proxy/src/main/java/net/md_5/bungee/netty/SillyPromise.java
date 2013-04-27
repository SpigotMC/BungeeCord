package net.md_5.bungee.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SillyPromise implements ChannelPromise
{

    private final Channel ch;

    @Override
    public Channel channel()
    {
        return ch;
    }

    @Override
    public ChannelPromise setSuccess(Void v)
    {
        return this;
    }

    @Override
    public ChannelPromise setSuccess()
    {
        return this;
    }

    @Override
    public boolean trySuccess()
    {
        return true;
    }

    @Override
    public ChannelPromise setFailure(Throwable thrwbl)
    {
        return this;

    }

    @Override
    public ChannelPromise addListener(GenericFutureListener<? extends Future<Void>> gl)
    {
        return this;
    }

    @Override
    public ChannelPromise addListeners(GenericFutureListener<? extends Future<Void>>... gls)
    {
        return this;

    }

    @Override
    public ChannelPromise removeListener(GenericFutureListener<? extends Future<Void>> gl)
    {
        return this;
    }

    @Override
    public ChannelPromise removeListeners(GenericFutureListener<? extends Future<Void>>... gls)
    {
        return this;
    }

    @Override
    public ChannelPromise sync() throws InterruptedException
    {
        return this;
    }

    @Override
    public ChannelPromise syncUninterruptibly()
    {
        return this;
    }

    @Override
    public ChannelPromise await() throws InterruptedException
    {
        return this;
    }

    @Override
    public ChannelPromise awaitUninterruptibly()
    {
        return this;
    }

    @Override
    public boolean isSuccess()
    {
        return true;
    }

    @Override
    public Throwable cause()
    {
        return null;
    }

    @Override
    public boolean await(long l, TimeUnit tu) throws InterruptedException
    {
        return true;
    }

    @Override
    public boolean await(long l) throws InterruptedException
    {
        return true;
    }

    @Override
    public boolean awaitUninterruptibly(long l, TimeUnit tu)
    {
        return true;
    }

    @Override
    public boolean awaitUninterruptibly(long l)
    {
        return true;
    }

    @Override
    public Void getNow()
    {
        return null;
    }

    @Override
    public boolean cancel(boolean bln)
    {
        return true;
    }

    @Override
    public boolean isCancelled()
    {
        return true;
    }

    @Override
    public boolean isDone()
    {
        return false;
    }

    @Override
    public Void get() throws InterruptedException, ExecutionException
    {
        return null;
    }

    @Override
    public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        return null;
    }

    @Override
    public boolean trySuccess(Void v)
    {
        return true;
    }

    @Override
    public boolean tryFailure(Throwable thrwbl)
    {
        return true;
    }
}
