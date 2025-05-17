package net.md_5.bungee.api.dialog.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.md_5.bungee.api.chat.BaseComponent;

@Data
@AllArgsConstructor
public class DialogAction
{

    private BaseComponent label;
    private BaseComponent tooltip;
    private int width;

    public DialogAction(BaseComponent label)
    {
        this( label, null, 150 );
    }
}
