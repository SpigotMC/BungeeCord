package net.md_5.bungee.api.config;

import lombok.Data;

@Data
public class TexturePackInfo
{

    /**
     * The URL of the texture pack.
     */
    private final String url;
    /**
     * The square dimension of this texture pack.
     */
    private final int size;
}
