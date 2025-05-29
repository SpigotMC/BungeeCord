package net.md_5.bungee.api.dialog.dynamic;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Executes a command. If the command requires a permission
 * higher than 0, a confirmation dialog will be shown by the client.
 */
@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RunCommand extends DynamicType
{

    /**
     * The template to be applied, where variables of the form
     * <code>$(key)</code> will be replaced by their
     * {@link net.md_5.bungee.api.dialog.input.DialogInput#key} value.
     */
    @NonNull
    private String template;

    public RunCommand(@NonNull String template)
    {
        super( "dynamic/run_command" );
        this.template = template;
    }
}
