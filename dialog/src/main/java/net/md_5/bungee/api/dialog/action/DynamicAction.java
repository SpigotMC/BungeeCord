package net.md_5.bungee.api.dialog.action;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.dialog.dynamic.DynamicType;

/**
 * Represents a static dialog action.
 */
@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DynamicAction extends DialogAction
{

    @NonNull
    private DynamicType action;

    public DynamicAction(@NonNull BaseComponent label, BaseComponent tooltip, Integer width, @NonNull DynamicType action)
    {
        super( label, tooltip, width );
        this.action = action;
    }

    public DynamicAction(@NonNull BaseComponent label, @NonNull DynamicType action)
    {
        super( label );
        this.action = action;
    }
}
