package net.md_5.bungee.api.dialog.action;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.dialog.submit.DialogSubmission;

/**
 * Represents a dialog button associated with the submission of a form dialog
 * containing inputs.
 */
@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DialogSubmitAction extends DialogAction
{

    /**
     * The ID of the button, used to distinguish submissions initiated via
     * different buttons on the dialog.
     */
    private String id;
    /**
     * The submission action to take.
     */
    @SerializedName("on_submit")
    private DialogSubmission onSubmit;

    public DialogSubmitAction(String id, DialogSubmission onSubmit, BaseComponent label)
    {
        this( id, onSubmit, label, null, 150 );
    }

    public DialogSubmitAction(String id, DialogSubmission onSubmit, BaseComponent label, BaseComponent tooltip, int width)
    {
        super( label, tooltip, width );
        this.id = id;
        this.onSubmit = onSubmit;
    }
}
