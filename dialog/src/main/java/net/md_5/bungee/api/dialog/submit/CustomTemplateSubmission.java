package net.md_5.bungee.api.dialog.submit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CustomTemplateSubmission extends DialogSubmission
{

    private String id;
    private String template;

    public CustomTemplateSubmission(String id, String template)
    {
        super( "minecraft:custom_template" );
        this.id = id;
        this.template = template;
    }
}
