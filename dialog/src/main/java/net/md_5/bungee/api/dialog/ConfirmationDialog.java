package net.md_5.bungee.api.dialog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.dialog.action.DialogClickAction;

@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Accessors(fluent = true)
public class ConfirmationDialog implements Dialog
{

    @Accessors(fluent = false)
    private DialogBase base;
    private DialogClickAction yes;
    private DialogClickAction no;

    public ConfirmationDialog(DialogBase base)
    {
        this( base, null, null );
    }
}
