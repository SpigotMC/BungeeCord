package net.md_5.bungee.api.dialog;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.dialog.action.DialogSubmitAction;
import net.md_5.bungee.api.dialog.input.DialogInput;

/**
 * Represents a dialog which contains a variety of inputs and a single submit
 * button at the bottom.
 */
@Data
@ToString
@EqualsAndHashCode
@Accessors(fluent = true)
public final class SimpleInputFormDialog implements Dialog
{

    @Accessors(fluent = false)
    private DialogBase base;
    /**
     * The inputs to the dialog. At least one input must be provided.
     */
    private List<DialogInput> inputs;
    /**
     * The action/submit buttons for the dialog.
     */
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
        Preconditions.checkArgument( inputs != null && !inputs.isEmpty(), "At least one input must be provided" );

        this.base = base;
        this.inputs = inputs;
        this.action = action;
    }
}
