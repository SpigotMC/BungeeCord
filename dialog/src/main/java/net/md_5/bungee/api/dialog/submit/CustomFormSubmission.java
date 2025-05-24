package net.md_5.bungee.api.dialog.submit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Submits the form with the given ID and all values as a payload.
 */
@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CustomFormSubmission extends DialogSubmission
{

    /**
     * The namespaced key of the submission.
     */
    @NonNull
    private String id;

    public CustomFormSubmission(@NonNull String id)
    {
        super( "minecraft:custom_form" );
        this.id = id;
    }
}
