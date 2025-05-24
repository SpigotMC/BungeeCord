package net.md_5.bungee.api.dialog.input;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Represents a single option (dropdown) input.
 */
@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SingleOptionInput extends DialogInput
{

    /**
     * The width of the input (default: 200, minimum: 1, maximum: 1024).
     */
    private int width;
    /**
     * The input label.
     */
    @NonNull
    private BaseComponent label;
    /**
     * Whether the label is visible (default: true).
     */
    @SerializedName("label_visible")
    private boolean labelVisible;
    /**
     * The non-empty list of options to be selected from.
     */
    @NonNull
    private List<InputOption> options;

    public SingleOptionInput(@NonNull String key, @NonNull BaseComponent label, @NonNull InputOption... options)
    {
        this( key, 200, label, true, Arrays.asList( options ) );
    }

    public SingleOptionInput(@NonNull String key, int width, @NonNull BaseComponent label, boolean labelVisible, @NonNull List<InputOption> options)
    {
        super( "minecraft:single_option", key );
        Preconditions.checkArgument( !options.isEmpty(), "At least one option must be provided" );

        width( width );
        this.label = label;
        this.labelVisible = labelVisible;
        this.options = options;
    }

    public SingleOptionInput width(int width)
    {
        Preconditions.checkArgument( width >= 1 && width <= 1024, "width must be between 1 and 1024" );
        this.width = width;
        return this;
    }
}
