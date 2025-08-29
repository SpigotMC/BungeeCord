package net.md_5.bungee.api.chat.objects;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class SpriteObject implements ChatObject
{
    /**
     * The namespaced ID of a sprite atlas, default value: minecraft:blocks
     */
    private String atlas;
    /**
     * The namespaced ID of a sprite in atlas, for example item/porkchop
     */
    private String sprite;
}
