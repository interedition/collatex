package eu.interedition.collatex.subst;

import eu.interedition.collatex.Witness;

public class LayeredWitness implements Witness {
    private String sigil;

    public LayeredWitness(String sigil) {
        this.sigil = sigil;
    }

    @Override
    public String getSigil() {
        return sigil;
    }
}