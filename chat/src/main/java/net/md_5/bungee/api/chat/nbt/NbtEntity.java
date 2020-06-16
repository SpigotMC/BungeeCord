package net.md_5.bungee.api.chat.nbt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NbtEntity
{

    private String name;
    private String type;
    private String id;
}
