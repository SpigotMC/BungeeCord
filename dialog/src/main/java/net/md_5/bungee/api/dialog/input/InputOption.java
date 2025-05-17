package net.md_5.bungee.api.dialog.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;

@Data
@AllArgsConstructor
@Accessors(fluent = true)
public class InputOption
{

    private String id;
    private BaseComponent display;
    private boolean initial;

    public InputOption(String id)
    {
        this( id, null, false );
    }
}
