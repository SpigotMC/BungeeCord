package net.md_5.bungee.api.dialog.action;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Represents a dialog action which will usually appear as a button.
 */
@Data
@Accessors(fluent = true)
public abstract class DialogAction
{

    /**
     * The text label of the button, mandatory.
     */
    @NonNull
    private BaseComponent label;
    /**
     * The hover tooltip of the button.
     */
    private BaseComponent tooltip;
    /**
     * The width of the button (default: 150, minimum: 1, maximum: 1024).
     */
    private Integer width;

    public DialogAction(@NonNull BaseComponent label, BaseComponent tooltip, Integer width)
    {
        this.label = label;
        this.tooltip = tooltip;
        setWidth( width );
    }

    public DialogAction(@NonNull BaseComponent label)
    {
        this( label, null, null );
    }

    public void setWidth(Integer width)
    {
        Preconditions.checkArgument( width == null || ( width >= 1 && width <= 1024 ), "width must be between 1 and 1024" );
        this.width = width;
    }
}
