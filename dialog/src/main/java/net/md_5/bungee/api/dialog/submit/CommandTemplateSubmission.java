package net.md_5.bungee.api.dialog.submit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Executes a command on form submission. If the command requires a permission
 * higher than 0, a confirmation dialog will be shown by the client.
 */
@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CommandTemplateSubmission extends DialogSubmission
{

    /**
     * The template to be applied, where variables of the form
     * <code>$(key)</code> will be replaced by their
     * {@link net.md_5.bungee.api.dialog.input.DialogInput#key} value.
     * <br>
     * The <code>action</code> key is special and will be replaced with the
     * {@link net.md_5.bungee.api.dialog.action.DialogSubmitAction#id}.
     */
    private String template;

    public CommandTemplateSubmission(String template)
    {
        super( "minecraft:command_template" );
        this.template = template;
    }
}
