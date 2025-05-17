package net.md_5.bungee.api.dialog.action;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.dialog.submit.DialogSubmission;

@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DialogSubmitAction extends DialogAction
{

    private String id;
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
