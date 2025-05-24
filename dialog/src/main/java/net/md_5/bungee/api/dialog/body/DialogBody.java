package net.md_5.bungee.api.dialog.body;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents the body content of a {@link net.md_5.bungee.api.dialog.Dialog}.
 */
@Data
@Accessors(fluent = true)
public abstract class DialogBody
{

    /**
     * The internal body type.
     */
    @NonNull
    @ApiStatus.Internal
    private final String type;
}
