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
 * Represents a dialog with text a list of action buttons grouped into columns
 * and scrollable if necessary.
 */
@Data
@ToString
@EqualsAndHashCode
@Accessors(fluent = true)
public final class MultiActionDialog implements Dialog
{

    @NonNull
    @Accessors(fluent = false)
    private DialogBase base;
    /**
     * The action buttons in the dialog. At least one must be provided.
     */
    @NonNull
    private List<ActionButton> actions;
    /**
     * The number of columns for the dialog buttons (default: 2).
     */
    private Integer columns;
    /**
     * The {@link ActionButton} activated when the dialog is exited.
     */
    @SerializedName("exit_action")
    private ActionButton exitAction;

    public MultiActionDialog(@NonNull DialogBase base, @NonNull ActionButton... actions)
    {
        this( base, Arrays.asList( actions ), null, null );
    }

    public MultiActionDialog(@NonNull DialogBase base, @NonNull List<ActionButton> actions, Integer columns, ActionButton exitAction)
    {
        Preconditions.checkArgument( !actions.isEmpty(), "At least one action must be provided" );

        this.base = base;
        this.actions = actions;
        columns( columns );
        this.exitAction = exitAction;
    }

    public MultiActionDialog columns(Integer columns)
    {
        Preconditions.checkArgument( columns == null || columns > 0, "At least one column is required" );
        this.columns = columns;
        return this;
    }
}
