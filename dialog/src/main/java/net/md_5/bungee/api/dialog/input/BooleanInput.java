package net.md_5.bungee.api.dialog.input;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Represents a checkbox input control.
 */
@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BooleanInput extends DialogInput
{

    /**
     * The input label.
     */
    @NonNull
    private BaseComponent label;
    /**
     * The initial value (default: false/unchecked).
     */
    private Boolean initial;
    /**
     * The string value to be submitted when true/checked (default: "true").
     */
    @SerializedName("on_true")
    private String onTrue;
    /**
     * The string value to be submitted when false/unchecked (default: "false").
     */
    @SerializedName("on_false")
    private String onFalse;

    public BooleanInput(@NonNull String key, @NonNull BaseComponent label)
    {
        this( key, label, null, "true", "false" );
    }

    public BooleanInput(@NonNull String key, @NonNull BaseComponent label, Boolean initial, String onTrue, String onFalse)
    {
        super( "minecraft:boolean", key );
        this.label = label;
        this.initial = initial;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }
}
