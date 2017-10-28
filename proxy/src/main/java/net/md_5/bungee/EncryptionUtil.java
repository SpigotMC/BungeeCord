package net.md_5.bungee;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import net.md_5.bungee.jni.NativeCode;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.jni.cipher.JavaCipher;
import net.md_5.bungee.jni.cipher.NativeCipher;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.EncryptionRequest;

/**
 * Class containing all encryption related methods for the proxy.
 */
public class EncryptionUtil
{

    private static final Random random = new Random();
    public static final KeyPair keys;
    @Getter
    private static final SecretKey secret = new SecretKeySpec( new byte[ 16 ], "AES" );
    public static final NativeCode<BungeeCipher> nativeFactory = new NativeCode<>( "native-cipher", JavaCipher.class, NativeCipher.class );

    static
    {
        try
        {
            KeyPairGenerator generator = KeyPairGenerator.getInstance( "RSA" );
            generator.initialize( 1024 );
            keys = generator.generateKeyPair();
        } catch ( NoSuchAlgorithmException ex )
        {
            throw new ExceptionInInitializerError( ex );
        }
    }

    public static EncryptionRequest encryptRequest()
    {
        String hash = Long.toString( random.nextLong(), 16 );
        byte[] pubKey = keys.getPublic().getEncoded();
        byte[] verify = new byte[ 4 ];
        random.nextBytes( verify );
        return new EncryptionRequest( hash, pubKey, verify );
    }

    public static SecretKey getSecret(EncryptionResponse resp, EncryptionRequest request) throws GeneralSecurityException
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

    public static BungeeCipher getCipher(boolean forEncryption, SecretKey shared) throws GeneralSecurityException
    {
        BungeeCipher cipher = nativeFactory.newInstance();

        cipher.init( forEncryption, shared );
        return cipher;
    }

    public static PublicKey getPubkey(EncryptionRequest request) throws GeneralSecurityException
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
