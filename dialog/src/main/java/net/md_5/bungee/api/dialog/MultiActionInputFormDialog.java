package net.md_5.bungee.api.dialog;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.dialog.action.DialogClickAction;
import net.md_5.bungee.api.dialog.input.DialogInput;

@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Accessors(fluent = true)
public class MultiActionInputFormDialog implements Dialog
{

    @Accessors(fluent = false)
    private DialogBase base;
    private List<DialogInput> inputs;
    private List<DialogClickAction> actions;

    public MultiActionInputFormDialog(DialogBase base)
    {
        this( base, null, null );
    }
}
