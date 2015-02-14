package eu.interedition.collatex.suffixarray;

/**
 * Symbol mappers (reversible int-coding).
 *
 * @author Michał Nowak (Carrot Search)
 * @author Dawid Weiss (Carrot Search)
 */
interface ISymbolMapper {
    void map(int[] input, int start, int length);

    void undo(int[] input, int start, int length);
}
