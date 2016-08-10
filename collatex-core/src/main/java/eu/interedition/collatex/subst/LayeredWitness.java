package eu.interedition.collatex.subst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;

public class LayeredWitness implements Iterable<Token>, Witness {
    private final String sigil;
    private final List<Token> tokens = new ArrayList<>();

    public LayeredWitness(String sigil) {
        this.sigil = sigil;
    }

    public LayeredWitness(String sigil, String content) {
        this(sigil);
        tokenize(content);
    }

    @Override
    public String getSigil() {
        return sigil;
    }

    @Override
    public Iterator<Token> iterator() {
        return Collections.unmodifiableList(tokens).iterator();
    }

    private void tokenize(String content) {

    }
}
