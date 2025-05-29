package net.md_5.bungee.api.dialog;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.dialog.body.DialogBody;
import net.md_5.bungee.api.dialog.input.DialogInput;

/**
 * Represents the title and other options common to all dialogs.
 */
@Data
@AllArgsConstructor
@Accessors(fluent = true)
public final class DialogBase
{

    /**
     * The mandatory dialog title.
     */
    @NonNull
    private BaseComponent title;
    /**
     * The name which is used for any buttons leading to this dialog (eg from a
     * {@link DialogListDialog}). Otherwise defaults to {@link #title}.
     */
    @SerializedName("external_title")
    private BaseComponent externalTitle;
    /**
     * The inputs to the dialog.
     */
    private List<DialogInput> inputs;
    /**
     * The body elements which make up this dialog.
     */
    private List<DialogBody> body;
    /**
     * Whether this dialog can be closed with the escape key (default: true).
     */
    @SerializedName("can_close_with_escape")
    private Boolean canCloseWithEscape;
    /**
     * Whether this dialog should pause the game in single-player mode (default:
     * true).
     */
    private Boolean pause;
    /**
     * Action to take after the a click or submit action is performed on the
     * dialog (default: close).
     */
    @SerializedName("after_action")
    private AfterAction afterAction;

    public DialogBase(@NonNull BaseComponent title)
    {
        this( title, null, null, null, null, null, null );
    }

    /**
     * Types of action which may be taken after the dialog.
     */
    public enum AfterAction
    {
        /**
         * Close the dialog.
         */
        @SerializedName("close")
        CLOSE,
        /**
         * Do nothing.
         */
        @SerializedName("none")
        NONE,
        /**
         * Show a waiting for response screen.
         */
        @SerializedName("wait_for_response")
        WAIT_FOR_RESPONSE;
    }
}
