package eu.interedition.collatex.suffixarray;

/**
 * A holder structure for a suffix array and longest common prefix array of
 * a given sequence. 
 *
 * @author Michał Nowak (Carrot Search)
 * @author Dawid Weiss (Carrot Search)
 */
public final class SuffixData
{
    private final int [] suffixArray;
    private final int [] lcp;

    SuffixData(int [] sa, int [] lcp)
    {
        this.suffixArray = sa;
        this.lcp = lcp;
    }

    public int [] getSuffixArray()
    {
        return suffixArray;
    }

    public int [] getLCP()
    {
        return lcp;
    }
}
