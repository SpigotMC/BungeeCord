package net.md_5.bungee.api.dialog;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.ClickEvent;

/**
 * Represents a dialog which shows the links configured/sent from the server.
 */
@Data
@ToString
@EqualsAndHashCode
@Accessors(fluent = true)
public final class ServerLinksDialog implements Dialog
{


    @NonNull
    @Accessors(fluent = false)
    private DialogBase base;
    /**
     * The optional {@link ClickEvent} for this dialog.
     */
    @SerializedName("on_click")
    private ClickEvent onClick;
    /**
     * The number of columns for the dialog buttons (default: 2).
     */
    private Integer columns;
    /**
     * The width of the dialog buttons (default: 150, minimum: 1, maximum: 1024).
     */
    @SerializedName("button_width")
    private Integer buttonWidth;

    public ServerLinksDialog(@NonNull DialogBase base)
    {
        this( base, null, null, null );
    }

    public ServerLinksDialog(@NonNull DialogBase base, ClickEvent onClick, Integer columns, Integer buttonWidth)
    {
        this.base = base;
        this.onClick = onClick;
        columns( columns );
        buttonWidth( buttonWidth );
    }

    public ServerLinksDialog columns(Integer columns)
    {
        Preconditions.checkArgument( columns == null || columns > 0, "At least one column is required" );
        this.columns = columns;
        return this;
    }

    public ServerLinksDialog buttonWidth(Integer buttonWidth)
    {
        Preconditions.checkArgument( buttonWidth== null || buttonWidth >= 1 && buttonWidth <= 1024, "buttonWidth must be between 1 and 1024" );
        this.buttonWidth = buttonWidth;
        return this;
    }
}
