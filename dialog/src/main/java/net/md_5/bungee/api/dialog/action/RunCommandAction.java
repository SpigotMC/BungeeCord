package net.md_5.bungee.api.dialog.action;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Executes a command. If the command requires a permission higher than 0, a
 * confirmation dialog will be shown by the client.
 */
@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
public class RunCommandAction implements Action
{

    /**
     * The template to be applied, where variables of the form
     * <code>$(key)</code> will be replaced by their
     * {@link net.md_5.bungee.api.dialog.input.DialogInput#key} value.
     */
    @NonNull
    private String template;
}
