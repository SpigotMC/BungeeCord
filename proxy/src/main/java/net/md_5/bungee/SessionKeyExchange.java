package net.md_5.bungee;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.XECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Random;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.protocol.packet.EncryptionRequest;

/**
 * Server side of the X25519 based session key exchange used by Minecraft 26.4
 * and later.
 * <p>
 * The proxy is always side "A" (the server), the client is always side "B".
 * Both sides derive two AES stream keys (one per direction), a key used for
 * the Mojang session server hash and a key used to verify that both sides saw
 * the same parameters from the shared X25519 secret via HKDF-SHA256.
 */
public final class SessionKeyExchange
{

    /**
     * Name the proxy uses for itself in the key exchange, sent to the client as
     * server id. Can be any string of at most 20 characters.
     */
    public static final String SERVER_NAME = "server";
    private static final NamedParameterSpec CURVE = NamedParameterSpec.X25519;
    private static final byte DIGEST_SEPARATOR = '+';
    private static final byte[] HKDF_SALT = Bytes.concat(
            Longs.toByteArray( 3195254534265565831L ),
            Longs.toByteArray( 8302438179253405948L ),
            Longs.toByteArray( 6794457759077931664L )
    );
    private static final char[] BASE32_HEX = "0123456789ABCDEFGHIJKLMNOPQRSTUV".toCharArray();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    //
    private final int protocolVersion;
    private final long salt;
    private final boolean authenticate;
    private final KeyPair keyPair;

    private SessionKeyExchange(int protocolVersion, long salt, boolean authenticate, KeyPair keyPair)
    {
        this.protocolVersion = protocolVersion;
        this.salt = salt;
        this.authenticate = authenticate;
        this.keyPair = keyPair;
    }

    /**
     * Creates a new server side key exchange with a fresh key pair and salt.
     *
     * @param protocolVersion the protocol version of the client
     * @param authenticate whether the client should authenticate with Mojang
     * @return the new key exchange
     * @throws GeneralSecurityException if X25519 is not available
     */
    public static SessionKeyExchange create(int protocolVersion, boolean authenticate) throws GeneralSecurityException
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance( "XDH" );
        generator.initialize( CURVE, SECURE_RANDOM );
        return new SessionKeyExchange( protocolVersion, SECURE_RANDOM.nextLong(), authenticate, generator.generateKeyPair() );
    }

    public EncryptionRequest createRequest()
    {
        return new EncryptionRequest( salt, SERVER_NAME, keyPair.getPublic().getEncoded(), null, authenticate );
    }

    /**
     * Derives the session keys from the public key of the client.
     *
     * @param clientName the name of the client as sent in the login start
     * packet
     * @param encodedClientKey the X.509 encoded X25519 public key of the client
     * @return the derived session keys
     * @throws GeneralSecurityException if the client key is invalid
     */
    public SessionKeys createSessionKeys(String clientName, byte[] encodedClientKey) throws GeneralSecurityException
    {
        PublicKey clientKey = KeyFactory.getInstance( "XDH" ).generatePublic( new X509EncodedKeySpec( encodedClientKey ) );
        Preconditions.checkArgument( clientKey instanceof XECPublicKey, "Not an XEC key" );
        AlgorithmParameterSpec params = ( (XECPublicKey) clientKey ).getParams();
        Preconditions.checkArgument( params instanceof NamedParameterSpec && ( (NamedParameterSpec) params ).getName().equals( CURVE.getName() ), "Wrong curve parameters" );

        // we are always side A, the client always side B
        Peer peerA = new Peer( SERVER_NAME, keyPair.getPublic().getEncoded() );
        Peer peerB = new Peer( clientName, clientKey.getEncoded() );

        KeyAgreement agreement = KeyAgreement.getInstance( "XDH" );
        agreement.init( keyPair.getPrivate(), CURVE );
        agreement.doPhase( clientKey, true );
        byte[] sharedSecret = agreement.generateSecret();

        byte[] prk = hmac( Bytes.concat( Longs.toByteArray( salt ), HKDF_SALT ), sharedSecret );
        String connectionParams = "a=" + peerA.name + ";b=" + peerB.name + ";protocol=" + protocolVersion;
        String authParams = "a=" + peerA.name + ";b=" + peerB.name;

        SecretKey aToB = new SecretKeySpec( hkdfExpand( prk, "a_to_b;" + connectionParams, 16 ), "AES" );
        SecretKey bToA = new SecretKeySpec( hkdfExpand( prk, "b_to_a;" + connectionParams, 16 ), "AES" );
        byte[] authKey = hkdfExpand( prk, "authenticate;" + authParams, 32 );
        byte[] integrityKey = hkdfExpand( prk, "verify;" + authParams, 32 );

        // integrity digest, the client sends the same value as signature
        Mac mac = newMac( integrityKey );
        mac.update( Ints.toByteArray( protocolVersion ) );
        mac.update( DIGEST_SEPARATOR );
        mac.update( Longs.toByteArray( salt ) );
        mac.update( DIGEST_SEPARATOR );
        mac.update( (byte) ( authenticate ? 1 : 2 ) );
        mac.update( DIGEST_SEPARATOR );
        peerA.addToDigest( mac );
        mac.update( DIGEST_SEPARATOR );
        peerB.addToDigest( mac );
        byte[] integrityDigest = mac.doFinal();

        // hash used as serverId for the Mojang session server
        mac = newMac( authKey );
        mac.update( peerA.encodedKey );
        mac.update( DIGEST_SEPARATOR );
        mac.update( peerB.encodedKey );
        String authenticationHash = base32Hex( Arrays.copyOf( mac.doFinal(), 25 ) );

        // the IVs of both directions are derived from the integrity digest
        Random ivRandom = new Random( Arrays.hashCode( integrityDigest ) );
        byte[] serverToClientIv = new byte[ 16 ];
        ivRandom.nextBytes( serverToClientIv );
        byte[] clientToServerIv = new byte[ 16 ];
        ivRandom.nextBytes( clientToServerIv );

        return new SessionKeys( integrityDigest, authenticationHash, aToB, new IvParameterSpec( serverToClientIv ), bToA, new IvParameterSpec( clientToServerIv ) );
    }

    private static Mac newMac(byte[] key) throws GeneralSecurityException
    {
        Mac mac = Mac.getInstance( "HmacSHA256" );
        mac.init( new SecretKeySpec( key, "HmacSHA256" ) );
        return mac;
    }

    private static byte[] hmac(byte[] key, byte[] data) throws GeneralSecurityException
    {
        return newMac( key ).doFinal( data );
    }

    /**
     * HKDF-Expand (RFC 5869) for output lengths of at most one hash block.
     *
     * @param prk the pseudo random key from the extract step
     * @param info the context info
     * @param length the output length, at most 32
     * @return the output key material
     * @throws GeneralSecurityException if HmacSHA256 is not available
     */
    static byte[] hkdfExpand(byte[] prk, String info, int length) throws GeneralSecurityException
    {
        Preconditions.checkArgument( length <= 32, "length > 32" );
        Mac mac = newMac( prk );
        mac.update( info.getBytes( StandardCharsets.UTF_8 ) );
        mac.update( (byte) 1 );
        return Arrays.copyOf( mac.doFinal(), length );
    }

    /**
     * Base32 with the extended hex alphabet (RFC 4648), the input length must
     * be a multiple of 5 so no padding is required.
     *
     * @param data the data to encode
     * @return the upper case encoded string
     */
    static String base32Hex(byte[] data)
    {
        Preconditions.checkArgument( data.length % 5 == 0, "length must be a multiple of 5" );
        StringBuilder sb = new StringBuilder( data.length * 8 / 5 );
        long buffer = 0;
        int bits = 0;
        for ( byte b : data )
        {
            buffer = ( buffer << 8 ) | ( b & 0xFF );
            bits += 8;
            while ( bits >= 5 )
            {
                bits -= 5;
                sb.append( BASE32_HEX[(int) ( buffer >> bits ) & 0x1F] );
            }
        }
        return sb.toString();
    }

    @RequiredArgsConstructor
    private static final class Peer
    {

        private final String name;
        private final byte[] encodedKey;

        private void addToDigest(Mac mac)
        {
            mac.update( name.getBytes( StandardCharsets.UTF_8 ) );
            mac.update( DIGEST_SEPARATOR );
            mac.update( encodedKey );
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static final class SessionKeys
    {

        /**
         * Expected value of the signature sent by the client.
         */
        private final byte[] integrityDigest;
        /**
         * Server id hash for the Mojang session server.
         */
        private final String authenticationHash;
        private final SecretKey serverToClientKey;
        private final IvParameterSpec serverToClientIv;
        private final SecretKey clientToServerKey;
        private final IvParameterSpec clientToServerIv;
    }
}
