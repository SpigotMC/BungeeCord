package net.md_5.bungee.api.dialog.input;

import com.google.common.base.Preconditions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Represents a number slider input.
 */
@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NumberRangeInput extends DialogInput
{

    /**
     * The width of the input (default: 200, minimum: 1, maximum: 1024).
     */
    private Integer width;
    /**
     * The label of the slider.
     */
    @NonNull
    private BaseComponent label;
    /**
     * A translate key used to display the label value (default:
     * options.generic_value).
     */
    private String labelFormat;
    /**
     * The start position of the slider (leftmost position).
     */
    private float start;
    /**
     * The end position of the slider (rightmost position).
     */
    private float end;
    /**
     * The steps in which the input will be increased or decreased, or null if
     * no specific steps.
     */
    private Float step;
    /**
     * The initial value of number input, or null to fall back to the middle.
     */
    private Float initial;

    public NumberRangeInput(@NonNull String key, @NonNull BaseComponent label, float start, float end)
    {
        this( key, null, label, "options.generic_value", start, end, null, null );
    }

    public NumberRangeInput(@NonNull String key, @NonNull BaseComponent label, float start, float end, Float step)
    {
        this( key, null, label, "options.generic_value", start, end, step, null );
    }

    public NumberRangeInput(@NonNull String key, @NonNull BaseComponent label, float start, float end, Float step, Float initial)
    {
        this( key, null, label, "options.generic_value", start, end, step, initial );
    }

    public NumberRangeInput(@NonNull String key, Integer width, @NonNull BaseComponent label, String labelFormat, float start, float end, Float step, Float initial)
    {
        super( "minecraft:number_range", key );
        width( width );
        this.label = label;
        this.labelFormat = labelFormat;
        this.start = start;
        this.end = end;
        step( step );
        initial( initial );
    }

    public NumberRangeInput width(Integer width)
    {
        Preconditions.checkArgument( width == null || ( width >= 1 && width <= 1024 ), "with must be between 1 and 1024" );
        this.width = width;
        return this;
    }

    public NumberRangeInput step(Float step)
    {
        Preconditions.checkArgument( step == null || step > 0, "step must be null or greater than zero" );
        this.step = step;
        return this;
    }

    public NumberRangeInput initial(Float initial)
    {
        // we need to calculate if the initial value is between start and end, regardless of the order
        float min = Math.min( start, end );
        float max = Math.max( start, end );
        Preconditions.checkArgument( initial == null || ( initial >= min && initial <= max ), "initial must be null or between start and end" );
        this.initial = initial;
        return this;
    }
}
