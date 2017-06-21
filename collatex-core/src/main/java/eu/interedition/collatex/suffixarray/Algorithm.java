package eu.interedition.collatex.suffixarray;

/**
 * An enum with constants indicating algorithms and their variants. This is of little
 * practical use, but a handy factory method {@link #getInstance()} is also provided.
 *
 * @author Michał Nowak (Carrot Search)
 * @author Dawid Weiss (Carrot Search)
 */
public enum Algorithm {
    /**
     * Karkkainen-Sanders.
     */
    SKEW("Kärkkäinen-Sanders"),

    /**
     * Karkkainen-Sanders, with decorators allowing arbitrary input.
     */
    SKEW_D("Kärkkäinen-Sanders (decorated for arbitrary input symbols)"),

    /**
     * Yuta Mori's divsufsort algorithm.
     */
    DIVSUFSORT("Mori's algorithm"),

    /**
     * Yuta Mori's implementation of SA-IS.
     */
    SAIS("SA-IS algorithm"),

    /**
     * Klaus-Bernd Schürmann's bucket pointer refinement algorithm
     */
    BPR("Klaus-Bernd Schürmann's bpr algorithm"),

    /**
     * Deep-Shallow algorithm by Manzini and Ferragina.
     */
    DEEP_SHALLOW("Manzini-Ferragina"),

    /**
     * "Larrson-Sadakane qsufsort algorithm
     */
    QSUFSORT("Larrson-Sadakane qsufsort algorithm");

    /**
     * Full name of the algorithm.
     */
    private final String name;

    /*
     *
     */
    Algorithm(String name) {
        this.name = name;
    }

    /**
     * @return Same as {@link #getInstance()}, but returns the algorithm instance
     * decorated to work with any range or distribution of input symbols
     * (respecting each algorithm's constraints).
     */
    public ISuffixArrayBuilder getDecoratedInstance() {
        switch (this) {
            case SKEW:
                return new DensePositiveDecorator(new ExtraTrailingCellsDecorator(
                    getInstance(), SuffixArrays.MAX_EXTRA_TRAILING_SPACE));

            default:
                return getInstance();
        }
    }

    /**
     * @return Create and return an algorithm instance.
     */
    public ISuffixArrayBuilder getInstance() {
        switch (this) {
            case SKEW:
                return new Skew();

            case DIVSUFSORT:
                return new DivSufSort();

            case SAIS:
                return new SAIS();

            case QSUFSORT:
                return new QSufSort();

            case BPR:
                return new BPR();

            case DEEP_SHALLOW:
                return new DeepShallow();

        }

        throw new RuntimeException("No algorithm for constant: " + this);
    }

    /**
     * If it is possbile, create memory conserving instance of an algorithm, <b>this
     * instance will overwrite input.</b>
     * <p>
     * If not, create default instance
     *
     * @return Create and return low memory consuming instance.
     */
    public ISuffixArrayBuilder getMemoryConservingInstance() {
        switch (this) {
            case QSUFSORT:
                return new QSufSort(false);
            case BPR:
                return new BPR(false);
            case DEEP_SHALLOW:
                return new DeepShallow(false);
            default:
                return getInstance();
        }
    }

    /**
     * Return the full name of the algorithm.
     */
    public String getName() {
        return name;
    }
}
