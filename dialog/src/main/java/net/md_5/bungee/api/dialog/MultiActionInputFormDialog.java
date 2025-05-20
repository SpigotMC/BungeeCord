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
 * Represents a dialog which contains a variety of inputs and a multiple submit
 * buttons at the bottom.
 */
@Data
@ToString
@EqualsAndHashCode
@Accessors(fluent = true)
public final class MultiActionInputFormDialog implements Dialog
{

    @Accessors(fluent = false)
    private DialogBase base;
    /**
     * The inputs to the dialog. At least one input must be provided.
     */
    private List<DialogInput> inputs;
    /**
     * The action/submit buttons for the dialog. At least one action must be
     * provided.
     */
    private List<DialogSubmitAction> actions;

    public MultiActionInputFormDialog(DialogBase base, DialogInput input, DialogSubmitAction action)
    {
        this( base, Arrays.asList( input ), Arrays.asList( action ) );
    }

    public MultiActionInputFormDialog(DialogBase base, List<DialogInput> inputs, List<DialogSubmitAction> actions)
    {
        Preconditions.checkArgument( inputs != null && !inputs.isEmpty(), "At least one input must be provided" );
        Preconditions.checkArgument( actions != null && !actions.isEmpty(), "At least one action must be provided" );

        this.base = base;
        this.inputs = inputs;
        this.actions = actions;
    }
}
