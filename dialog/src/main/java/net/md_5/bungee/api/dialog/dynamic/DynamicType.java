package net.md_5.bungee.api.dialog.dynamic;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents an action which may be taken on form dialog submission.
 */
@Data
@Accessors(fluent = true)
public class DynamicType
{

    /**
     * The internal submissions type.
     */
    @NonNull
    @ApiStatus.Internal
    private final String type;
}
