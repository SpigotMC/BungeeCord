/*
 * The original file is licensed under the following license:
 *
 * Copyright 2016 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * ---
 *
 * This file is partly based on the io/netty/handler/flush/FlushConsolidationHandler.java file from Netty (v4.1).
 * It was modified to fit to bungee's use of forwarded connections.
 * All modifications are licensed under the "Modified BSD 3-Clause License" to be found at https://github.com/SpigotMC/BungeeCord/blob/master/LICENSE
 */
package net.md_5.bungee.netty.flush;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * Signalises a read loop is currently ongoing to {@link BungeeFlushConsolidationHandler}.
 */
public final class FlushSignalingHandler extends ChannelDuplexHandler
{
    private BungeeFlushConsolidationHandler target;

    public FlushSignalingHandler(BungeeFlushConsolidationHandler target)
    {
        this.target = target;
    }

    public void setTarget(BungeeFlushConsolidationHandler target)
    {
        // flush old target
        this.target.resetReadAndFlushIfNeeded( this.target.ctx );
        // set new target
        this.target = target;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
    {
        // This may be the last event in the read loop, so flush now!
        target.resetReadAndFlushIfNeeded( target.ctx );
        ctx.fireChannelReadComplete();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        target.readInProgress = true;
        ctx.fireChannelRead( msg );
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        // To ensure we not miss to flush anything, do it now.
        target.resetReadAndFlushIfNeeded( target.ctx );
        ctx.fireExceptionCaught( cause );
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception
    {
        // Try to flush one last time if flushes are pending before disconnect the channel.
        target.resetReadAndFlushIfNeeded( target.ctx );
        ctx.disconnect( promise );
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception
    {
        // Try to flush one last time if flushes are pending before close the channel.
        target.resetReadAndFlushIfNeeded( target.ctx );
        ctx.close( promise );
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
    {
        target.flushIfNeeded( target.ctx );
    }
}
