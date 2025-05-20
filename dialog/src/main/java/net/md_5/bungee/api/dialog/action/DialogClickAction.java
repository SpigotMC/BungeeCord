package net.md_5.bungee.api.dialog.action;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;

/**
 * Represents a button which may be clicked.
 */
@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DialogClickAction extends DialogAction
{

    /**
     * The optional action to take on click.
     */
    @SerializedName("on_click")
    private ClickEvent onClick;

    public DialogClickAction(BaseComponent label)
    {
        this( null, label );
    }

    public DialogClickAction(ClickEvent onClick, BaseComponent label)
    {
        this( onClick, label, null, 150 );
    }

    public DialogClickAction(ClickEvent onClick, BaseComponent label, BaseComponent tooltip, int width)
    {
        super( label, tooltip, width );
        this.onClick = onClick;
    }
}
