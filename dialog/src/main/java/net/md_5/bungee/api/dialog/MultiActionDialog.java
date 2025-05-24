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
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.dialog.action.DialogClickAction;

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
    private List<DialogClickAction> actions;
    /**
     * The number of columns for the dialog buttons (default: 2).
     */
    private Integer columns;
    /**
     * The {@link ClickEvent} activated when the dialog is cancelled.
     */
    @SerializedName("on_cancel")
    private ClickEvent onCancel;

    public MultiActionDialog(@NonNull DialogBase base, @NonNull DialogClickAction... actions)
    {
        this( base, Arrays.asList( actions ), 2, null );
    }

    public MultiActionDialog(@NonNull DialogBase base, @NonNull List<DialogClickAction> actions, Integer columns, ClickEvent onCancel)
    {
        Preconditions.checkArgument( !actions.isEmpty(), "At least one action must be provided" );

        this.base = base;
        this.actions = actions;
        columns( columns );
        this.onCancel = onCancel;
    }

    public MultiActionDialog columns(Integer columns)
    {
        Preconditions.checkArgument( columns == null || columns > 0, "At least one column is required" );
        this.columns = columns;
        return this;
    }
}
