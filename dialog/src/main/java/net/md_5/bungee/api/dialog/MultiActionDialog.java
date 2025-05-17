package net.md_5.bungee.api.dialog;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.dialog.action.DialogClickAction;

@Data
@ToString
@EqualsAndHashCode
@Accessors(fluent = true)
public class MultiActionDialog implements Dialog
{

    @Accessors(fluent = false)
    private DialogBase base;
    private List<DialogClickAction> actions;
    private int columns;
    @SerializedName("on_cancel")
    private ClickEvent onCancel;

    public MultiActionDialog(DialogBase base, DialogClickAction... actions)
    {
        this( base, Arrays.asList( actions ), 2, null );
    }

    public MultiActionDialog(DialogBase base, List<DialogClickAction> actions, int columns, ClickEvent onCancel)
    {
        Preconditions.checkArgument( actions != null && !actions.isEmpty(), "At least one action must be provided" );

        this.base = base;
        this.actions = actions;
        this.columns = columns;
        this.onCancel = onCancel;
    }
}
