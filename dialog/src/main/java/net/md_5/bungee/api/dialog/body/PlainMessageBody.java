package net.md_5.bungee.api.dialog.body;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Represents a dialog body which consists of text constrained to a certain
 * width.
 */
@Data
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
    private int width;

    public PlainMessageBody(@NonNull BaseComponent contents)
    {
        this( contents, 200 );
    }

    public PlainMessageBody(@NonNull BaseComponent contents, int width)
    {
        super( "minecraft:plain_message" );
        this.contents = contents;
        setWidth( width );
    }

    public void setWidth(int width)
    {
        Preconditions.checkArgument( width >= 1 && width <= 1024, "width must be between 1 and 1024");
        this.width = width;
    }
}
