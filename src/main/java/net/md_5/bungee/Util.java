package net.md_5.bungee;

import java.net.InetSocketAddress;

public class Util {

    private static final int DEFAULT_PORT = 25565;

    /**
     * Basic method to transform human readable addresses into usable address
     * objects.
     *
     * @param hostline in the format of 'host:port'
     * @return the constructed hostname + port.
     */
    public static InetSocketAddress getAddr(String hostline) {
        String[] split = hostline.split(":");
        if (split.length < 2) {
            throw new IllegalArgumentException("Invalid split format");
        }
        int port = DEFAULT_PORT;
        if (split.length > 1) {
            port = Integer.parseInt(split[1]);
        }
        return new InetSocketAddress(split[0], port);
    }

    /**
     * Turns a InetSocketAddress into a string that can be reconstructed later.
     *
     * @param address the address to serialize
     * @return
     */
    public static String getAddr(InetSocketAddress address) {
        return address.getAddress().getHostAddress() + ((address.getPort() != DEFAULT_PORT) ? ":" + address.getPort() : "");
    }

    /**
     * Get the packet id of specified byte array.
     */
    public static int getId(byte[] b) {
        return b[0] & 0xFF;
    }

    /**
     * Normalizes a config path by prefix upper case letters with '_' and
     * turning them to lowercase.
     *
     * @param s the string to normalize
     * @return the normalized path
     */
    public static String normalize(String s) {
        StringBuilder result = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) {
                result.append("_");
            }
            result.append(Character.toLowerCase(c));
        }
        return result.toString();
    }

    public static String hex(int i) {
        return String.format("0x%02X", i);
    }

    public static String exception(Throwable t) {
        return t.getClass().getSimpleName() + " : " + t.getMessage() + " @ " + t.getStackTrace()[0].getFileName() + ":" + t.getStackTrace()[0].getLineNumber();
    }
}
