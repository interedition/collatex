package eu.interedition.collatex.suffixarray;

/**
 * Holder for minimum and maximum.
 * 
 * @see Tools#minmax(int[],int,int)
 *
 * @author Michał Nowak (Carrot Search)
 * @author Dawid Weiss (Carrot Search)
 */
final class MinMax
{
    public final int min;
    public final int max;
    
    MinMax(int min, int max)
    {
        this.min = min;
        this.max = max;
    }

    public int range()
    {
        return max - min;
    }
}
