package net.md_5.bungee.api.dialog;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.ClickEvent;

@Data
@ToString
@EqualsAndHashCode
@Accessors(fluent = true)
public class DialogListDialog implements Dialog
{

    @Accessors(fluent = false)
    private DialogBase base;
    private List<Dialog> dialogs;
    @SerializedName("on_cancel")
    private ClickEvent onCancel;
    private int columns;
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
