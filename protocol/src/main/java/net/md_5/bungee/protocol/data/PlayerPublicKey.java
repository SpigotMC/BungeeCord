package net.md_5.bungee.protocol.data;

import lombok.Data;

@Data
public class PlayerPublicKey
{

    private final long expiry;
    private final byte[] key;
    private final byte[] signature;
}
