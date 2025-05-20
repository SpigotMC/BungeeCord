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
    @SerializedName("max_length")
    private int maxLength;
    /**
     * if set, allows users to input multiple lines
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

    @Data
    @Accessors(fluent = true)
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = false)
    public static class Multiline {
        /**
         * The maximum length of input
         */
        @SerializedName("max_lines")
        private Integer maxLines;
        private Integer height = 32;
    }
}
