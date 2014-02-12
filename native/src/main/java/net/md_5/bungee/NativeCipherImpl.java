package net.md_5.bungee;

class NativeCipherImpl
{

    /**
     * Initializes the key.
     *
     * @param key the key to for encryption
     * @return the pointer to key
     */
    native long init(byte[] key);

    /**
     * Frees the key.
     *
     * @param key the pointer to key
     */
    native void free(long key);

    /**
     * This method will encrypt some data in AES-CFB8 using the specified key.
     *
     * @param forEncryption encryption / decryption mode
     * @param key the pointer to key
     * @param iv the iv to use
     * @param in the starting memory address for reading data
     * @param out the starting memory address for writing data
     * @param length the length of data to read / write
     */
    native void cipher(boolean forEncryption, long key, byte[] iv, long in, long out, int length);
}
