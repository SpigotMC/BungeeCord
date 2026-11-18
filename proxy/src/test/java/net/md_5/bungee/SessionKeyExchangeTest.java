package net.md_5.bungee;

import static org.junit.jupiter.api.Assertions.*;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.NamedParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Random;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import org.junit.jupiter.api.Test;

public class SessionKeyExchangeTest
{

    private static final byte[] HKDF_SALT = Bytes.concat(
            Longs.toByteArray( 3195254534265565831L ),
            Longs.toByteArray( 8302438179253405948L ),
            Longs.toByteArray( 6794457759077931664L )
    );

    @Test
    public void testHkdfRfc5869() throws Exception
    {
        // RFC 5869 test case 1 (extract)
        byte[] ikm = new byte[ 22 ];
        Arrays.fill( ikm, (byte) 0x0b );
        byte[] prk = hmac( HexFormat.of().parseHex( "000102030405060708090a0b0c" ), ikm );
        assertEquals( "077709362c2e32df0ddc3f0dc47bba6390b6c73bb50f9c3122ec844ad7c2b3e5", HexFormat.of().formatHex( prk ) );

        // RFC 5869 test case 3 (empty salt and info), first 32 bytes of the output
        prk = hmac( new byte[ 32 ], ikm );
        assertEquals( "19ef24a32c717b167f33a91d6f648bdf96596776afdb6377ac434c1c293ccb04", HexFormat.of().formatHex( prk ) );
        byte[] okm = SessionKeyExchange.hkdfExpand( prk, "", 32 );
        assertEquals( "8da4e775a563c18f715f802a063c5a31b8a11f5c5ee1879ec3454e5f3c738d2d", HexFormat.of().formatHex( okm ) );
    }

    @Test
    public void testBase32Hex()
    {
        // RFC 4648 test vectors
        assertEquals( "", SessionKeyExchange.base32Hex( new byte[ 0 ] ) );
        assertEquals( "CPNMUOJ1", SessionKeyExchange.base32Hex( "fooba".getBytes( StandardCharsets.US_ASCII ) ) );
        assertEquals( 40, SessionKeyExchange.base32Hex( new byte[ 25 ] ).length() );
    }

    @Test
    public void testHandshake() throws Exception
    {
        int protocol = ProtocolConstants.MINECRAFT_26_4;
        String clientName = "Notch";

        SessionKeyExchange server = SessionKeyExchange.create( protocol, true );
        EncryptionRequest request = roundTrip( server.createRequest(), new EncryptionRequest(), protocol );
        assertEquals( SessionKeyExchange.SERVER_NAME, request.getServerId() );

        // client side, mirrors the vanilla client implementation
        KeyPairGenerator generator = KeyPairGenerator.getInstance( "XDH" );
        generator.initialize( NamedParameterSpec.X25519 );
        KeyPair clientPair = generator.generateKeyPair();
        PublicKey serverKey = KeyFactory.getInstance( "XDH" ).generatePublic( new X509EncodedKeySpec( request.getPublicKey() ) );

        KeyAgreement agreement = KeyAgreement.getInstance( "XDH" );
        agreement.init( clientPair.getPrivate(), NamedParameterSpec.X25519 );
        agreement.doPhase( serverKey, true );
        byte[] prk = hmac( Bytes.concat( Longs.toByteArray( request.getSalt() ), HKDF_SALT ), agreement.generateSecret() );

        String params = "a=" + request.getServerId() + ";b=" + clientName;
        byte[] aToB = expand( prk, "a_to_b;" + params + ";protocol=" + protocol, 16 );
        byte[] bToA = expand( prk, "b_to_a;" + params + ";protocol=" + protocol, 16 );
        byte[] authKey = expand( prk, "authenticate;" + params, 32 );
        byte[] verifyKey = expand( prk, "verify;" + params, 32 );

        Mac mac = Mac.getInstance( "HmacSHA256" );
        mac.init( new SecretKeySpec( verifyKey, "HmacSHA256" ) );
        mac.update( Bytes.concat( Ints.toByteArray( protocol ), new byte[]
        {
            '+'
        }, Longs.toByteArray( request.getSalt() ), new byte[]
        {
            '+', (byte) ( request.isShouldAuthenticate() ? 1 : 2 ), '+'
        } ) );
        mac.update( ( request.getServerId() + "+" ).getBytes( StandardCharsets.UTF_8 ) );
        mac.update( serverKey.getEncoded() );
        mac.update( (byte) '+' );
        mac.update( ( clientName + "+" ).getBytes( StandardCharsets.UTF_8 ) );
        mac.update( clientPair.getPublic().getEncoded() );
        byte[] signature = mac.doFinal();

        mac.init( new SecretKeySpec( authKey, "HmacSHA256" ) );
        mac.update( serverKey.getEncoded() );
        mac.update( (byte) '+' );
        mac.update( clientPair.getPublic().getEncoded() );
        byte[] authDigest = Arrays.copyOf( mac.doFinal(), 25 );

        Random ivRandom = new Random( Arrays.hashCode( signature ) );
        byte[] ivServerToClient = new byte[ 16 ];
        ivRandom.nextBytes( ivServerToClient );
        byte[] ivClientToServer = new byte[ 16 ];
        ivRandom.nextBytes( ivClientToServer );

        EncryptionResponse response = new EncryptionResponse();
        response.setPublicKey( clientPair.getPublic().getEncoded() );
        response.setSignature( signature );
        response = roundTrip( response, new EncryptionResponse(), protocol );

        // server side
        SessionKeyExchange.SessionKeys keys = server.createSessionKeys( clientName, response.getPublicKey() );
        assertArrayEquals( signature, keys.getIntegrityDigest() );
        assertEquals( SessionKeyExchange.base32Hex( authDigest ), keys.getAuthenticationHash() );
        assertArrayEquals( aToB, keys.getServerToClientKey().getEncoded() );
        assertArrayEquals( bToA, keys.getClientToServerKey().getEncoded() );
        assertArrayEquals( ivServerToClient, keys.getServerToClientIv().getIV() );
        assertArrayEquals( ivClientToServer, keys.getClientToServerIv().getIV() );

        // the stream ciphers of both sides must match
        byte[] plain = "Hello 26.4".getBytes( StandardCharsets.UTF_8 );
        BungeeCipher serverEncrypt = EncryptionUtil.getCipher( true, keys.getServerToClientKey(), keys.getServerToClientIv() );
        BungeeCipher clientDecrypt = EncryptionUtil.getCipher( false, new SecretKeySpec( aToB, "AES" ), new IvParameterSpec( ivServerToClient ) );
        assertArrayEquals( plain, cipher( clientDecrypt, cipher( serverEncrypt, plain ) ) );

        BungeeCipher clientEncrypt = EncryptionUtil.getCipher( true, new SecretKeySpec( bToA, "AES" ), new IvParameterSpec( ivClientToServer ) );
        BungeeCipher serverDecrypt = EncryptionUtil.getCipher( false, keys.getClientToServerKey(), keys.getClientToServerIv() );
        assertArrayEquals( plain, cipher( serverDecrypt, cipher( clientEncrypt, plain ) ) );

        // a different client name must lead to different keys
        assertFalse( Arrays.equals( signature, server.createSessionKeys( "Other", response.getPublicKey() ).getIntegrityDigest() ) );
    }

    private static byte[] cipher(BungeeCipher cipher, byte[] in) throws Exception
    {
        ByteBuf out = Unpooled.buffer();
        cipher.cipher( Unpooled.wrappedBuffer( in ), out );
        byte[] result = new byte[ out.readableBytes() ];
        out.readBytes( result );
        return result;
    }

    private static <T extends net.md_5.bungee.protocol.DefinedPacket> T roundTrip(T packet, T target, int protocol)
    {
        ByteBuf buf = Unpooled.buffer();
        packet.write( buf, ProtocolConstants.Direction.TO_CLIENT, protocol );
        target.read( buf, ProtocolConstants.Direction.TO_CLIENT, protocol );
        assertEquals( 0, buf.readableBytes() );
        return target;
    }

    private static byte[] hmac(byte[] key, byte[] data) throws Exception
    {
        Mac mac = Mac.getInstance( "HmacSHA256" );
        mac.init( new SecretKeySpec( key, "HmacSHA256" ) );
        return mac.doFinal( data );
    }

    private static byte[] expand(byte[] prk, String info, int length) throws Exception
    {
        return Arrays.copyOf( hmac( prk, Bytes.concat( info.getBytes( StandardCharsets.UTF_8 ), new byte[]
        {
            1
        } ) ), length );
    }
}
