package net.md_5.bungee.api.dialog.body;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.md_5.bungee.api.chat.BaseComponent;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PlainMessageBody extends DialogBody
{

    private BaseComponent contents;
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
