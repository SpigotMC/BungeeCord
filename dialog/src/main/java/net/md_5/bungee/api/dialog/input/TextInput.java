package net.md_5.bungee.api.dialog.input;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Represents a textbox input.
 */
@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TextInput extends DialogInput
{

    /**
     * The width of this text input (default: 200, minimum: 1, maximum: 1024).
     */
    private Integer width;
    /**
     * The label of this text input.
     */
    @NonNull
    private BaseComponent label;
    /**
     * The visibility of this text input's label.
     */
    @SerializedName("label_visible")
    private Boolean labelVisible;
    /**
     * The initial value of this text input.
     */
    private String initial;
    /**
     * The maximum length of the input (default: 32).
     */
    @SerializedName("max_length")
    private Integer maxLength;
    /**
     * If present, allows users to input multiple lines.
     */
    private Multiline multiline;

    public TextInput(@NonNull String key, @NonNull BaseComponent label)
    {
        this( key, null, label, null, null, null, null );
    }

    public TextInput(@NonNull String key, Integer width, @NonNull BaseComponent label, Boolean labelVisible, String initial, Integer maxLength)
    {
        this( key, width, label, labelVisible, initial, maxLength, null );
    }

    public TextInput(@NonNull String key, Integer width, @NonNull BaseComponent label, Boolean labelVisible, String initial, Integer maxLength, Multiline multiline)
    {
        super( "minecraft:text", key );
        width( width );
        this.label = label;
        this.labelVisible = labelVisible;
        this.initial = initial;
        this.maxLength = maxLength;
        this.multiline = multiline;
    }

    /**
     * Configuration data for a multiline input.
     */
    @Data
    @NoArgsConstructor
    @Accessors(fluent = true)
    public static class Multiline
    {

        /**
         * The maximum length of input, or null to disable any limits.
         */
        @SerializedName("max_lines")
        private Integer maxLines;
        /**
         * The height of this input (default: 32, minimum: 1, maximum: 512).
         */
        private Integer height;

        public Multiline(Integer maxLines, Integer height)
        {
            height( height ).maxLines( maxLines );
        }

        public Multiline height(Integer height)
        {
            Preconditions.checkArgument( height == null || height >= 1 && height <= 512, "height must null or be between 1 and 512" );
            this.height = height;
            return this;
        }
    }

    public TextInput width(Integer width)
    {
        Preconditions.checkArgument( width == null || ( width >= 1 && width <= 1024 ), "width must be between 1 and 1024" );
        this.width = width;
        return this;
    }
}
