package net.md_5.bungee.api.dialog.input;

import lombok.Data;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a type of input which may be displayed/submitted with a form
 * dialog.
 */
@Data
@Accessors(fluent = true)
public class DialogInput
{

    /**
     * The internal input type.
     */
    @ApiStatus.Internal
    private final String type;
    /**
     * The key corresponding to this input and associated with the value
     * submitted.
     */
    private final String key;
}
