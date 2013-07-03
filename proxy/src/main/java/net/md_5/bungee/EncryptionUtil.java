package net.md_5.bungee;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import net.md_5.bungee.protocol.packet.PacketFCEncryptionResponse;
import net.md_5.bungee.protocol.packet.PacketFDEncryptionRequest;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Class containing all encryption related methods for the proxy.
 */
public class EncryptionUtil
{

    private static final Random random = new Random();
    public static KeyPair keys;
    @Getter
    private static SecretKey secret = new SecretKeySpec( new byte[ 16 ], "AES" );

    static
    {
        Security.addProvider( new BouncyCastleProvider() );
        try
        {
            keys = KeyPairGenerator.getInstance( "RSA" ).generateKeyPair();
        } catch ( NoSuchAlgorithmException ex )
        {
            throw new ExceptionInInitializerError( ex );
        }
    }

    public static PacketFDEncryptionRequest encryptRequest()
    {
        String hash = ( BungeeCord.getInstance().config.isOnlineMode() ) ? Long.toString( random.nextLong(), 16 ) : "-";
        byte[] pubKey = keys.getPublic().getEncoded();
        byte[] verify = new byte[ 4 ];
        random.nextBytes( verify );
        return new PacketFDEncryptionRequest( hash, pubKey, verify );
    }

    public static SecretKey getSecret(PacketFCEncryptionResponse resp, PacketFDEncryptionRequest request) throws GeneralSecurityException
    {
        Cipher cipher = Cipher.getInstance( "RSA" );
        cipher.init( Cipher.DECRYPT_MODE, keys.getPrivate() );
        byte[] decrypted = cipher.doFinal( resp.getVerifyToken() );

        if ( !Arrays.equals( request.getVerifyToken(), decrypted ) )
        {
            throw new IllegalStateException( "Key pairs do not match!" );
        }

        cipher.init( Cipher.DECRYPT_MODE, keys.getPrivate() );
        return new SecretKeySpec( cipher.doFinal( resp.getSharedSecret() ), "AES" );
    }

    public static BufferedBlockCipher getCipher(boolean forEncryption, Key shared)
    {
        BufferedBlockCipher cip = new BufferedBlockCipher( new CFBBlockCipher( new AESFastEngine(), 8 ) );
        cip.init( forEncryption, new ParametersWithIV( new KeyParameter( shared.getEncoded() ), shared.getEncoded(), 0, 16 ) );
        return cip;
    }

    public static PublicKey getPubkey(PacketFDEncryptionRequest request) throws GeneralSecurityException
    {
        return KeyFactory.getInstance( "RSA" ).generatePublic( new X509EncodedKeySpec( request.getPublicKey() ) );
    }

    public static byte[] encrypt(Key key, byte[] b) throws GeneralSecurityException
    {
        Cipher hasher = Cipher.getInstance( "RSA" );
        hasher.init( Cipher.ENCRYPT_MODE, key );
        return hasher.doFinal( b );
    }
}
