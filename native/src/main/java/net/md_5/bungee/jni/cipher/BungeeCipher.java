package net.md_5.bungee.jni.cipher;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;

/**
 * Class to expose cipher methods from either native or fallback Java cipher.
 */
public interface BungeeCipher
{

    void init(boolean forEncryption, SecretKey key) throws GeneralSecurityException;

    void free();

    void cipher(ByteBuf in, ByteBuf out) throws GeneralSecurityException;

    ByteBuf cipher(ChannelHandlerContext ctx, ByteBuf in) throws GeneralSecurityException;

    /*
     * This indicates whether the input ByteBuf is allowed to be a CompositeByteBuf.
     * If you need access to a memory address, you should not allow composite buffers.
     */
    boolean allowComposite();
}
