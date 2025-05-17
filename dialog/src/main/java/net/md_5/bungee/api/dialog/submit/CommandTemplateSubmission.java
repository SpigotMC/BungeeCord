package net.md_5.bungee.api.dialog.submit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CommandTemplateSubmission extends DialogSubmission
{

    private String template;

    public CommandTemplateSubmission(String template)
    {
        super( "minecraft:command_template" );
        this.template = template;
    }
}
