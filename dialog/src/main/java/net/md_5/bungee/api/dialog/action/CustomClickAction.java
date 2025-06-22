package net.md_5.bungee.api.dialog.action;

import com.google.gson.JsonElement;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Submits the dialog with the given ID and values as a payload.
 */
@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
public class CustomClickAction implements Action
{

    /**
     * The namespaced key of the submission.
     */
    @NonNull
    private String id;
    /**
     * Fields to be added to the submission payload.
     */
    private JsonElement additions;
}
