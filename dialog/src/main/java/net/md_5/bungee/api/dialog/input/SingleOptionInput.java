package net.md_5.bungee.api.dialog.input;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;

@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SingleOptionInput extends DialogInput
{

    private int width;
    private BaseComponent label;
    @SerializedName("label_visible")
    private boolean labelVisible;
    private List<InputOption> options;

    public SingleOptionInput(String key, BaseComponent label, InputOption... options)
    {
        this( key, 200, label, true, Arrays.asList( options ) );
    }

    public SingleOptionInput(String key, int width, BaseComponent label, boolean labelVisible, List<InputOption> options)
    {
        super( "minecraft:single_option", key );
        this.width = width;
        this.label = label;
        this.labelVisible = labelVisible;
        this.options = options;
    }
}
