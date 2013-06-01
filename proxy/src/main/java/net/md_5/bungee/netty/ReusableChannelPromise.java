package net.md_5.bungee.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;

@RequiredArgsConstructor
public class ReusableChannelPromise implements ChannelPromise
{

    private final Channel ch;

    @Override
    public Channel channel()
    {
        return ch;
    }

    @Override
    public ChannelPromise setSuccess(Void result)
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ChannelPromise setSuccess()
    {
        return this;
    }

    @Override
    public boolean trySuccess()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ChannelPromise setFailure(Throwable cause)
    {
        return this;
    }

    @Override
    public ChannelPromise addListener(GenericFutureListener<? extends Future<Void>> listener)
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ChannelPromise addListeners(GenericFutureListener<? extends Future<Void>>... listeners)
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ChannelPromise removeListener(GenericFutureListener<? extends Future<Void>> listener)
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ChannelPromise removeListeners(GenericFutureListener<? extends Future<Void>>... listeners)
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ChannelPromise sync() throws InterruptedException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ChannelPromise syncUninterruptibly()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ChannelPromise await() throws InterruptedException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public ChannelPromise awaitUninterruptibly()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean isSuccess()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Throwable cause()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean await(long timeoutMillis) throws InterruptedException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit)
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMillis)
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Void getNow()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean isCancelled()
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean isDone()
    {
        return false;
    }

    @Override
    public Void get() throws InterruptedException, ExecutionException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean trySuccess(Void result)
    {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

    @Override
    public boolean tryFailure(Throwable cause)
    {
        ProxyServer.getInstance().getLogger().log( Level.WARNING, "Exception in tryFailure(..)", cause );
        return true;
    }
}
