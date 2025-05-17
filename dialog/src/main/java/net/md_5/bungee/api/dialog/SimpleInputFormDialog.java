package net.md_5.bungee.api.dialog;

import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.dialog.action.DialogSubmitAction;
import net.md_5.bungee.api.dialog.input.DialogInput;

@Data
@ToString
@EqualsAndHashCode
@Accessors(fluent = true)
public class SimpleInputFormDialog implements Dialog
{

    @Accessors(fluent = false)
    private DialogBase base;
    private List<DialogInput> inputs;
    private DialogSubmitAction action;

    public SimpleInputFormDialog(DialogBase base, DialogInput... inputs)
    {
        this( base, null, inputs );
    }

    public SimpleInputFormDialog(DialogBase base, DialogSubmitAction action, DialogInput... inputs)
    {
        this( base, action, Arrays.asList( inputs ) );
    }

    public SimpleInputFormDialog(DialogBase base, DialogSubmitAction action, List<DialogInput> inputs)
    {
        this.base = base;
        this.inputs = inputs;
        this.action = action;
    }
}
