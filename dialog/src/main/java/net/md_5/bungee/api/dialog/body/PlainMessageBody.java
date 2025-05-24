package net.md_5.bungee.api.dialog.body;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Represents a dialog body which consists of text constrained to a certain
 * width.
 */
@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PlainMessageBody extends DialogBody
{

    /**
     * The text body.
     */
    @NonNull
    private BaseComponent contents;
    /**
     * The maximum width (default: 200, minimum: 1, maximum: 1024).
     */
    private Integer width;

    public PlainMessageBody(@NonNull BaseComponent contents)
    {
        this( contents, null );
    }

    public PlainMessageBody(@NonNull BaseComponent contents, Integer width)
    {
        super( "minecraft:plain_message" );
        this.contents = contents;
        width( width );
    }

    public void width(Integer width)
    {
        Preconditions.checkArgument( width == null || ( width >= 1 && width <= 1024 ), "width must be between 1 and 1024" );
        this.width = width;
    }
}
