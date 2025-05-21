package net.md_5.bungee.api.dialog;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.ClickEvent;

/**
 * Represents a dialog which contains buttons that link to other dialogs.
 */
@Data
@ToString
@EqualsAndHashCode
@Accessors(fluent = true)
public final class DialogListDialog implements Dialog
{

    @Accessors(fluent = false)
    private DialogBase base;
    /**
     * The child dialogs behind each button.
     */
    private List<Dialog> dialogs;
    /**
     * The {@link ClickEvent} activated when the dialog is cancelled.
     */
    @SerializedName("on_cancel")
    private ClickEvent onCancel;
    /**
     * The number of columns for the dialog buttons (default: 2).
     */
    private int columns;
    /**
     * The width of the dialog buttons (default: 150, minimum: 1, maximum: 1024).
     */
    @SerializedName("button_width")
    private int buttonWidth;

    public DialogListDialog(DialogBase base, Dialog... dialogs)
    {
        this( base, Arrays.asList( dialogs ), null, 2, 150 );
    }

    public DialogListDialog(DialogBase base, List<Dialog> dialogs, ClickEvent onCancel, int columns, int buttonWidth)
    {
        this.base = base;
        this.dialogs = dialogs;
        this.onCancel = onCancel;
        this.columns = columns;
        this.buttonWidth = buttonWidth;
    }
}
