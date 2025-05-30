package net.md_5.bungee.api.dialog.action;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a static dialog action.
 */
@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class StaticAction extends DialogAction
{

    @NonNull
    private ChatClickEventWrapper action;

    public StaticAction(@NonNull BaseComponent label, BaseComponent tooltip, Integer width, @NonNull ClickEvent action)
    {
        super( label, tooltip, width );
        this.action = new ChatClickEventWrapper( action );
    }

    public StaticAction(@NonNull BaseComponent label, @NonNull ClickEvent action)
    {
        super( label );
        this.action = new ChatClickEventWrapper( action );
    }

    @Data
    @ApiStatus.Internal
    public static final class ChatClickEventWrapper
    {

        private final ClickEvent event;
    }
}
