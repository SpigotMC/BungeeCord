package net.md_5.bungee.jni.cipher;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.security.GeneralSecurityException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Class to expose cipher methods from either native or fallback Java cipher.
 */
public interface BungeeCipher
{

    void init(boolean forEncryption, SecretKey key) throws GeneralSecurityException;

    /**
     * Initializes this cipher with a separate initialization vector instead of
     * reusing the key as initialization vector.
     *
     * @param forEncryption whether to encrypt or decrypt
     * @param key the AES key
     * @param iv the initialization vector
     * @throws GeneralSecurityException if the cipher could not be initialized
     * @throws UnsupportedOperationException if this implementation does not
     * support a separate initialization vector
     */
    default void init(boolean forEncryption, SecretKey key, IvParameterSpec iv) throws GeneralSecurityException
    {
        throw new UnsupportedOperationException( getClass().getName() + " does not support a separate IV" );
    }

    void free();

    void cipher(ByteBuf in, ByteBuf out) throws GeneralSecurityException;

    ByteBuf cipher(ChannelHandlerContext ctx, ByteBuf in) throws GeneralSecurityException;

    /*
     * This indicates whether the input ByteBuf is allowed to be a CompositeByteBuf.
     * If you need access to a memory address, you should not allow composite buffers.
     */
    boolean allowComposite();
}
