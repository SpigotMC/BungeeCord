package net.md_5.bungee.api.dialog.input;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.md_5.bungee.api.chat.BaseComponent;

@Data
@Accessors(fluent = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NumberRangeInput extends DialogInput
{

    private int width;
    private BaseComponent label;
    private String labelFormat;
    private int start;
    private int end;
    private int steps;
    private int initial;

    public NumberRangeInput(String key, BaseComponent label, int start, int end, int steps)
    {
        this( key, 200, label, "options.generic_value", start, end, steps, start );
    }

    public NumberRangeInput(String key, int width, BaseComponent label, String labelFormat, int start, int end, int steps, int initial)
    {
        super( "minecraft:number_range", key );
        this.width = width;
        this.label = label;
        this.labelFormat = labelFormat;
        this.start = start;
        this.end = end;
        this.steps = steps;
        this.initial = initial;
    }
}
