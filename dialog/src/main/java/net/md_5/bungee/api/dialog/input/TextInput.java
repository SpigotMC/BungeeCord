package net.md_5.bungee.api.dialog.input;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;

@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TextInput extends DialogInput
{

    private int width;
    private BaseComponent label;
    @SerializedName("label_visible")
    private boolean labelVisible;
    private String initial;

    public TextInput(String key, BaseComponent label)
    {
        this( key, 200, label, true, "" );
    }

    public TextInput(String key, int width, BaseComponent label, boolean labelVisible, String initial)
    {
        super( "minecraft:text", key );
        this.width = width;
        this.label = label;
        this.labelVisible = labelVisible;
        this.initial = initial;
    }
}
