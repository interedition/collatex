package eu.interedition.collatex.suffixtree;

/**
 * Represents the terminating item of a sequence.
 *
 * @author <a href="https://github.com/maxgarfinkel/suffixTree">Max Garfinkel</a>
 */
class SequenceTerminal<S> {

    private final S sequence;

    SequenceTerminal(S sequence) {
        this.sequence = sequence;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
      return !(o == null || o.getClass() != this.getClass()) && ((SequenceTerminal<S>) o).sequence.equals(this.sequence);
    }

    public int hashCode() {
        return sequence.hashCode();
    }

    @Override
    public String toString() {
        return "$" + sequence.toString() + "$";
    }

    public S getSequence() {
        return sequence;
    }

}
