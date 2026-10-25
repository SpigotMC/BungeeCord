package net.md_5.bungee.api.dialog.action;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.ClickEvent;

/**
 * Represents a static dialog action.
 */
@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
public class StaticAction implements Action
{

    @NonNull
    private ClickEvent clickEvent;
}
