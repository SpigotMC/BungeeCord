package ru.leymooo.fakeonline;

/**
 *
 * @author Leymooo
 */
public class Range
{

    private int lowerBound;
    private int upperBound;

    public Range(int lowerBound, int upperBound)
    {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public boolean isBetween(int value)
    {
        return ( this.lowerBound <= value && value <= this.upperBound );
    }

}
