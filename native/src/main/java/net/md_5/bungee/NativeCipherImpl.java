package net.md_5.bungee;

class NativeCipherImpl
{

    /**
     * Initializes the key.
     *
     * @param key the key to for encryption
     * @return the pointer to key
     */
    native long initKey(byte[] key);

    /**
     * Initializes the iv.
     *
     * @param iv the iv
     * @return the pointer to iv
     */
    native long initIV(byte[] iv);

    /**
     * Frees the key.
     *
     * @param key the pointer to key
     * @param iv the pointer to iv
     */
    native void free(long key, long iv);

    /**
     * This method will encrypt some data in AES-CFB8 using the specified key.
     *
     * @param forEncryption encryption / decryption mode
     * @param key the pointer to key
     * @param iv the pointer to iv
     * @param in the starting memory address for reading data
     * @param out the starting memory address for writing data
     * @param length the length of data to read / write
     */
    native void cipher(boolean forEncryption, long key, long iv, long in, long out, int length);
}
