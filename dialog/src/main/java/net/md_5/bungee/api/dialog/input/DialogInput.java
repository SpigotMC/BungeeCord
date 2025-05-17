package net.md_5.bungee.api.dialog.input;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class DialogInput
{

    private final String type;
    private final String key;
}
