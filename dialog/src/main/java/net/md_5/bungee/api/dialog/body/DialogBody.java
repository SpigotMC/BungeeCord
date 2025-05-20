package net.md_5.bungee.api.dialog.body;

import lombok.Data;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents the body content of a {@link net.md_5.bungee.api.dialog.Dialog}.
 */
@Data
public abstract class DialogBody
{

    /**
     * The internal body type.
     */
    @ApiStatus.Internal
    private final String type;
}
