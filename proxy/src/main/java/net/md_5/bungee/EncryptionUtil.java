package net.md_5.bungee;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Longs;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import net.md_5.bungee.jni.NativeCode;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.jni.cipher.JavaCipher;
import net.md_5.bungee.jni.cipher.NativeCipher;
import net.md_5.bungee.protocol.PlayerPublicKey;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;

/**
 * Class containing all encryption related methods for the proxy.
 */
public class EncryptionUtil
{

    private static final Random random = new Random();
    private static final Base64.Encoder MIME_ENCODER = Base64.getMimeEncoder( 76, "\n".getBytes( StandardCharsets.UTF_8 ) );
    public static final KeyPair keys;
    @Getter
    private static final SecretKey secret = new SecretKeySpec( new byte[ 16 ], "AES" );
    public static final NativeCode<BungeeCipher> nativeFactory = new NativeCode<>( "native-cipher", JavaCipher::new, NativeCipher::new );
    private static final PublicKey MOJANG_KEY;

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

        try
        {
            MOJANG_KEY = KeyFactory.getInstance( "RSA" ).generatePublic( new X509EncodedKeySpec( ByteStreams.toByteArray( EncryptionUtil.class.getResourceAsStream( "/yggdrasil_session_pubkey.der" ) ) ) );
        } catch ( IOException | NoSuchAlgorithmException | InvalidKeySpecException ex )
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

    public static boolean check(PlayerPublicKey publicKey, UUID uuid) throws GeneralSecurityException
    {
        Signature signature = Signature.getInstance( "SHA1withRSA" );
        signature.initVerify( MOJANG_KEY );

        byte[] check;
        if ( uuid != null )
        {
            byte[] encoded = getPubkey( publicKey.getKey() ).getEncoded();
            check = new byte[ 24 + encoded.length ];

            ByteBuffer.wrap( check ).order( ByteOrder.BIG_ENDIAN ).putLong( uuid.getMostSignificantBits() ).putLong( uuid.getLeastSignificantBits() ).putLong( publicKey.getExpiry() ).put( encoded );
        } else
        {
            check = ( publicKey.getExpiry() + "-----BEGIN RSA PUBLIC KEY-----\n" + MIME_ENCODER.encodeToString( getPubkey( publicKey.getKey() ).getEncoded() ) + "\n-----END RSA PUBLIC KEY-----\n" ).getBytes( StandardCharsets.US_ASCII );
        }
        signature.update( check );

        return signature.verify( publicKey.getSignature() );
    }

    public static boolean check(PlayerPublicKey publicKey, EncryptionResponse resp, EncryptionRequest request) throws GeneralSecurityException
    {
        if ( publicKey != null )
        {
            Signature signature = Signature.getInstance( "SHA256withRSA" );
            signature.initVerify( getPubkey( publicKey.getKey() ) );

            signature.update( request.getVerifyToken() );
            signature.update( Longs.toByteArray( resp.getEncryptionData().getSalt() ) );

            return signature.verify( resp.getEncryptionData().getSignature() );
        } else
        {
            Cipher cipher = Cipher.getInstance( "RSA" );
            cipher.init( Cipher.DECRYPT_MODE, keys.getPrivate() );
            byte[] decrypted = cipher.doFinal( resp.getVerifyToken() );

            return Arrays.equals( request.getVerifyToken(), decrypted );
        }
    }

    public static SecretKey getSecret(EncryptionResponse resp, EncryptionRequest request) throws GeneralSecurityException
    {
        Cipher cipher = Cipher.getInstance( "RSA" );
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
        return getPubkey( request.getPublicKey() );
    }

    private static PublicKey getPubkey(byte[] b) throws GeneralSecurityException
    {
        return KeyFactory.getInstance( "RSA" ).generatePublic( new X509EncodedKeySpec( b ) );
    }

    public static byte[] encrypt(Key key, byte[] b) throws GeneralSecurityException
    {
        Cipher hasher = Cipher.getInstance( "RSA" );
        hasher.init( Cipher.ENCRYPT_MODE, key );
        return hasher.doFinal( b );
    }
}
