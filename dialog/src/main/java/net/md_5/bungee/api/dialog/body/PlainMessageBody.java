package net.md_5.bungee.api.dialog.body;

import lombok.Data;
import lombok.EqualsAndHashCode;
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
    private BaseComponent contents;
    /**
     * The maximum width (default: 200, minimum: 1, maximum: 1024).
     */
    private int width;

    public PlainMessageBody(BaseComponent contents)
    {
        this( contents, 200 );
    }

    public PlainMessageBody(BaseComponent contents, int width)
    {
        super( "minecraft:plain_message" );
        this.contents = contents;
        this.width = width;
    }
}
