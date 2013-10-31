package net.md_5.bungee;

class NativeCipherImpl
{
    /**
     * This method will encrypt some data in AES-CFB8 using the specified key.
     *
     * @param key the key to use for encryption
     * @param iv the iv to use
     * @param in the starting memory address for reading data
     * @param out the starting memory address for writing data
     * @param length the length of data to read / write
     */
    native void cipher(boolean forEncryption, byte[] key, byte[] iv, long in, long out, int length);
}
