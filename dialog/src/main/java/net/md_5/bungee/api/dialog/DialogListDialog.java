package net.md_5.bungee.api.dialog;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.dialog.action.ActionButton;

/**
 * Represents a dialog which contains buttons that link to other dialogs.
 */
@Data
@ToString
@EqualsAndHashCode
@Accessors(fluent = true)
public final class DialogListDialog implements Dialog
{

    @NonNull
    @Accessors(fluent = false)
    private DialogBase base;
    /**
     * The child dialogs behind each button.
     */
    private List<Dialog> dialogs;
    /**
     * The {@link ActionButton} activated when the dialog is exited.
     */
    @SerializedName("exit_action")
    private ActionButton exitAction;
    /**
     * The number of columns for the dialog buttons (default: 2).
     */
    private Integer columns;
    /**
     * The width of the dialog buttons (default: 150, minimum: 1, maximum:
     * 1024).
     */
    @SerializedName("button_width")
    private Integer buttonWidth;

    public DialogListDialog(@NonNull DialogBase base, Dialog... dialogs)
    {
        this( base, Arrays.asList( dialogs ), null, null, null );
    }

    public DialogListDialog(@NonNull DialogBase base, List<Dialog> dialogs, ActionButton exitAction, Integer columns, Integer buttonWidth)
    {
        this.base = base;
        this.dialogs = dialogs;
        this.exitAction = exitAction;
        columns( columns );
        buttonWidth( buttonWidth );
    }

    public DialogListDialog columns(Integer columns)
    {
        Preconditions.checkArgument( columns == null || columns > 0, "At least one column is required" );
        this.columns = columns;
        return this;
    }

    public DialogListDialog buttonWidth(Integer buttonWidth)
    {
        Preconditions.checkArgument( buttonWidth == null || ( buttonWidth >= 1 && buttonWidth <= 1024 ), "buttonWidth must be between 1 and 1024" );
        this.buttonWidth = buttonWidth;
        return this;
    }
}
