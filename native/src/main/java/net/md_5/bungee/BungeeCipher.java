package net.md_5.bungee;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import java.security.GeneralSecurityException;

/**
 * Class to expose cipher methods from either native or fallback Java cipher.
 */
public interface BungeeCipher
{
    public void init(boolean forEncryption, SecretKey key) throws GeneralSecurityException;

    public void cipher(ByteBuf in, ByteBuf out) throws GeneralSecurityException;

    public ByteBuf cipher(ChannelHandlerContext ctx, ByteBuf in) throws GeneralSecurityException;
}
