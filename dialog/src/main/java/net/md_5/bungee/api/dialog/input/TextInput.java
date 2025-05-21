package net.md_5.bungee.api.dialog.input;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
    private int width;
    /**
     * The label of this text input.
     */
    private BaseComponent label;
    /**
     * The visibility of this text input's label.
     */
    @SerializedName("label_visible")
    private boolean labelVisible;
    /**
     * The initial value of this text input.
     */
    private String initial;
    /**
     * The maximum length of the input (default: 32).
     */
    @SerializedName("max_length")
    private int maxLength;
    /**
     * If present, allows users to input multiple lines.
     */
    private Multiline multiline;

    public TextInput(String key, BaseComponent label)
    {
        this( key, 200, label, true, null, 32, null );
    }

    public TextInput(String key, int width, BaseComponent label, boolean labelVisible, String initial, Integer maxLength)
    {
        this( key, width, label, labelVisible, initial, maxLength, null );
    }

    public TextInput(String key, int width, BaseComponent label, boolean labelVisible, String initial, Integer maxLength, Multiline multiline)
    {
        super( "minecraft:text", key );
        this.width = width;
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
        private Integer height = 32;
    }
}
