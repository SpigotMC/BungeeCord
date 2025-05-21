package net.md_5.bungee.api.dialog.submit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Submits the form with the given ID and a single payload specified by the
 * template.
 */
@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CustomTemplateSubmission extends DialogSubmission
{

    /**
     * The namespaced key of the submission.
     */
    private String id;
    /**
     * The template to be applied, where variables of the form
     * <code>$(key)</code> will be replaced by their
     * {@link net.md_5.bungee.api.dialog.input.DialogInput#key} value.
     * <br>
     * The <code>action</code> key is special and will be replaced with the
     * {@link net.md_5.bungee.api.dialog.action.DialogSubmitAction#id}.
     */
    private String template;

    public CustomTemplateSubmission(String id, String template)
    {
        super( "minecraft:custom_template" );
        this.id = id;
        this.template = template;
    }
}
