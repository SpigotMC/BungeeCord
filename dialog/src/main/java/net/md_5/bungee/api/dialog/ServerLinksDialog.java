package net.md_5.bungee.api.dialog;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.ClickEvent;

@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Accessors(fluent = true)
public class ServerLinksDialog implements Dialog
{

    @Accessors(fluent = false)
    private DialogBase base;
    @SerializedName("on_click")
    private ClickEvent onClick;
    private int columns;
    @SerializedName("button_width")
    private int buttonWidth;

    public ServerLinksDialog(DialogBase base)
    {
        this( base, null, 2, 150 );
    }
}
