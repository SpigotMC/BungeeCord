package net.md_5.bungee.api.dialog.input;

import lombok.Data;
import lombok.EqualsAndHashCode;
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
    private int width;
    /**
     * The label of the slider.
     */
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

    public NumberRangeInput(String key, BaseComponent label, float start, float end)
    {
        this( key, 200, label, "options.generic_value", start, end, null, null );
    }

    public NumberRangeInput(String key, BaseComponent label, float start, float end, Float step)
    {
        this( key, 200, label, "options.generic_value", start, end, step, null );
    }

    public NumberRangeInput(String key, BaseComponent label, float start, float end, Float step, Float initial)
    {
        this( key, 200, label, "options.generic_value", start, end, step, initial );
    }

    public NumberRangeInput(String key, int width, BaseComponent label, String labelFormat, float start, float end, Float step, Float initial)
    {
        super( "minecraft:number_range", key );
        this.width = width;
        this.label = label;
        this.labelFormat = labelFormat;
        this.start = start;
        this.end = end;
        this.step = step;
        this.initial = initial;
    }
}
