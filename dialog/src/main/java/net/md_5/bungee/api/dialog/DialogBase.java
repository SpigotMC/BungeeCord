package net.md_5.bungee.api.dialog;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.dialog.body.DialogBody;

/**
 * Represents the title and other options common to all dialogs.
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Accessors(fluent = true)
public final class DialogBase
{

    /**
     * The mandatory dialog title.
     */
    private final BaseComponent title;
    /**
     * The name which is used for any buttons leading to this dialog (eg from a
     * {@link DialogListDialog}). Otherwise defaults to {@link #title}.
     */
    @SerializedName("external_title")
    private BaseComponent externalTitle;
    /**
     * The body elements which make up this dialog.
     */
    private List<DialogBody> body;
    /**
     * Whether this dialog can be closed with the escape key (default: true).
     */
    @SerializedName("can_close_with_escape")
    private boolean canCloseWithEscape = true;
}
