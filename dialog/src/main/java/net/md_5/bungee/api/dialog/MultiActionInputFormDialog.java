package net.md_5.bungee.api.dialog;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
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
    @NonNull
    private DialogBase base;
    /**
     * The inputs to the dialog. At least one input must be provided.
     */
    @NonNull
    private List<DialogInput> inputs;
    /**
     * The action/submit buttons for the dialog. At least one action must be
     * provided.
     */
    @NonNull
    private List<DialogSubmitAction> actions;
    /**
     * The amount of columns (default: 2)
     */
    private Integer columns;

    public MultiActionInputFormDialog(@NonNull DialogBase base, @NonNull DialogInput input, @NonNull DialogSubmitAction action)
    {
        this( base, Arrays.asList( input ), Arrays.asList( action ), 2 );
    }

    public MultiActionInputFormDialog(@NonNull DialogBase base, @NonNull DialogInput input, @NonNull DialogSubmitAction action, Integer columns)
    {
        this( base, Arrays.asList( input ), Arrays.asList( action ), columns );
    }

    public MultiActionInputFormDialog(@NonNull DialogBase base, @NonNull List<DialogInput> inputs, @NonNull List<DialogSubmitAction> actions, Integer columns)
    {
        Preconditions.checkArgument( !inputs.isEmpty(), "At least one input must be provided" );
        Preconditions.checkArgument( !actions.isEmpty(), "At least one action must be provided" );

        this.base = base;
        this.inputs = inputs;
        this.actions = actions;
        columns( columns );
    }

    public MultiActionInputFormDialog columns(Integer columns)
    {
        Preconditions.checkArgument( columns == null || columns > 0, "At least one column is required" );
        this.columns = columns;
        return this;
    }
}
