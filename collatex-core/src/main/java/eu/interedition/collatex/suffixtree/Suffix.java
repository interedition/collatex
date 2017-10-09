package eu.interedition.collatex.suffixtree;

/**
 * Represents the remaining suffix to be inserted during suffix tree
 * construction. This is essentially a start and end pointer into the
 * underlying sequence. This is like a kind of sliding window where the head
 * can never fall behind the tail, and the tail can never fall behind the head.
 *
 * @param <T>
 * @author <a href="https://github.com/maxgarfinkel/suffixTree">Max Garfinkel</a>
 */
class Suffix<T, S extends Iterable<T>> {
    private int start;
    private int end;
    private Sequence<T, S> sequence;

    /**
     * Construct a subsequence of sequence. The subsequence will be a suffix of
     * the sequence UP TO the point in the sequence we have reached whilst
     * running Ukonnen's algorithm. In this sense it is not a true suffix of the
     * sequence but only a suffix of the portion of the sequence we have so far
     * parsed.
     *
     * @param start    The start position of the suffix within the sequence
     * @param end      The end position of the suffix within the sequence
     * @param sequence The main sequence
     */
    public Suffix(int start, int end, Sequence<T, S> sequence) {
        testStartAndEndValues(start, end);
        testStartEndAgainstSequenceLength(start, end, sequence.getLength());
        this.start = start;
        this.end = end;
        this.sequence = sequence;
    }

    private void testStartEndAgainstSequenceLength(int start, int end, int sequenceLength) {
        if (start > sequenceLength || end > sequenceLength)
            throw new IllegalArgumentException("Suffix start and end must be less than or equal to sequence length");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[(");
        sb.append(start).append(", ").append(end).append(")");
        int end = getEndPosition();
        for (int i = start; i < end; i++) {
            sb.append(sequence.getItem(i)).append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * @return The position in the master sequence of the end item in this
     * suffix. This value is inclusive, thus and end of 0 implies the
     * suffix contains only the item at <code>sequence[0]</code>
     */
    int getEndPosition() {
        return end;
    }

    /**
     * Get the end item of this suffix.
     *
     * @return The end item of sequence
     */
    Object getEndItem() {
        if (isEmpty())
            return null;
        return sequence.getItem(end - 1);
    }

    /**
     * Get the start of this suffix.
     *
     * @return
     */
    Object getStart() {
        if (isEmpty())
            return null;
        return sequence.getItem(start);
    }

    /**
     * Decrement the length of this suffix. This is done by incrementing the
     * start position. This is reducing its length from the back.
     */
    void decrement() {
        if (start == end)
            increment();
        start++;
    }

    /**
     * Increments the length of the suffix by incrementing the end position. The
     * effectivly moves the suffix forward, along the master sequence.
     */
    void increment() {
        end++;
        if (end > sequence.getLength())
            throw new IndexOutOfBoundsException("Incremented suffix beyond end of sequence");

    }

    /**
     * Indicates if the suffix is empty.
     *
     * @return
     */
    boolean isEmpty() {
        return start >= end || end > sequence.getLength();
    }

    /**
     * Retrieves the count of remaining items in the suffix.
     *
     * @return The number of items in the suffix.
     */
    int getRemaining() {
        if (isEmpty())
            return 0;

        return (end - start);
    }

    /**
     * Retrieves the item the given distance from the end of the suffix.
     *
     * @param distanceFromEnd The distance from the end.
     * @return The item the given distance from the end.
     * @throws IllegalArgumentException if the distance from end is greater than the length of the
     *                                  suffix.
     */
    public Object getItemXFromEnd(int distanceFromEnd) {
        if ((end - (distanceFromEnd)) < start) {
            throw new IllegalArgumentException(distanceFromEnd
                + " extends before the start of this suffix: ");
        }
        return sequence.getItem(end - distanceFromEnd);
    }

    void reset(int start, int end) {
        testStartAndEndValues(start, end);
        this.start = start;
        this.end = end;
    }

    private void testStartAndEndValues(int start, int end) {
        if (start < 0 || end < 0)
            throw new IllegalArgumentException("You cannot set a suffix start or end to less than zero.");
        if (end < start)
            throw new IllegalArgumentException("A suffix end position cannot be less than its start position.");
    }
}
