package net.md_5.bungee.api.dialog;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.ClickEvent;

/**
 * Represents a dialog which shows the links configured/sent from the server.
 */
@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Accessors(fluent = true)
public final class ServerLinksDialog implements Dialog
{

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
    private int columns;
    /**
     * The width of the dialog buttons (default: 150, minimum: 1, maximum: 1024).
     */
    @SerializedName("button_width")
    private int buttonWidth;

    public ServerLinksDialog(DialogBase base)
    {
        this( base, null, 2, 150 );
    }
}
