package net.md_5.bungee.api.dialog.submit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CustomFormSubmission extends DialogSubmission
{

    private String id;

    public CustomFormSubmission(String id)
    {
        super( "minecraft:custom_form" );
        this.id = id;
    }
}
