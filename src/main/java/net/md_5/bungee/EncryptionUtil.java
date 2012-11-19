package net.md_5.bungee;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Random;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import net.md_5.bungee.packet.PacketFCEncryptionResponse;
import net.md_5.bungee.packet.PacketFDEncryptionRequest;
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
    private static KeyPair keys;

    static
    {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static PacketFDEncryptionRequest encryptRequest() throws NoSuchAlgorithmException
    {
        if (keys == null)
        {
            keys = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        }

        String hash = Long.toString(random.nextLong(), 16);
        byte[] pubKey = keys.getPublic().getEncoded();
        byte[] verify = new byte[4];
        random.nextBytes(verify);
        return new PacketFDEncryptionRequest(hash, pubKey, verify);
    }

    public static SecretKey getSecret(PacketFCEncryptionResponse resp, PacketFDEncryptionRequest request) throws BadPaddingException, IllegalBlockSizeException, IllegalStateException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException
    {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, keys.getPrivate());
        byte[] decrypted = cipher.doFinal(resp.verifyToken);

        if (!Arrays.equals(request.verifyToken, decrypted))
        {
            throw new IllegalStateException("Key pairs do not match!");
        }

        cipher.init(Cipher.DECRYPT_MODE, keys.getPrivate());
        byte[] shared = resp.sharedSecret;
        byte[] secret = cipher.doFinal(shared);

        return new SecretKeySpec(secret, "AES");
    }

    public static boolean isAuthenticated(String username, String connectionHash, SecretKey shared) throws NoSuchAlgorithmException, IOException
    {
        String encName = URLEncoder.encode(username, "UTF-8");

        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        for (byte[] bit : new byte[][]
                {
                    connectionHash.getBytes("ISO_8859_1"), shared.getEncoded(), keys.getPublic().getEncoded()
                })
        {
            sha.update(bit);
        }

        String encodedHash = URLEncoder.encode(new BigInteger(sha.digest()).toString(16), "UTF-8");
        String authURL = "http://session.minecraft.net/game/checkserver.jsp?user=" + encName + "&serverId=" + encodedHash;
        String reply;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(authURL).openStream())))
        {
            reply = in.readLine();
        }

        return "YES".equals(reply);
    }

    public static BufferedBlockCipher getCipher(boolean forEncryption, Key shared)
    {
        BufferedBlockCipher cip = new BufferedBlockCipher(new CFBBlockCipher(new AESFastEngine(), 8));
        cip.init(forEncryption, new ParametersWithIV(new KeyParameter(shared.getEncoded()), shared.getEncoded()));
        return cip;
    }

    public static SecretKey getSecret()
    {
        byte[] rand = new byte[16];
        random.nextBytes(rand);
        return new SecretKeySpec(rand, "AES");
    }

    public static PublicKey getPubkey(PacketFDEncryptionRequest request) throws InvalidKeySpecException, NoSuchAlgorithmException
    {
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(request.publicKey));
    }

    public static byte[] encrypt(Key key, byte[] b) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException
    {
        Cipher hasher = Cipher.getInstance("RSA");
        hasher.init(Cipher.ENCRYPT_MODE, key);
        return hasher.doFinal(b);
    }

    public static byte[] getShared(SecretKey key, PublicKey pubkey) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException
    {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubkey);
        return cipher.doFinal(key.getEncoded());
    }
}
