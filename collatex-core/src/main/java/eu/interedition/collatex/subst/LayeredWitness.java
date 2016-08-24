package eu.interedition.collatex.subst;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleWitness;

public class LayeredWitness extends SimpleWitness/* implements Iterable<Token>, Witness */ {
    // private final String sigil;
    private List<Token> tokens = new ArrayList<>();

    @Override
    public List<Token> getTokens() {
        return tokens;
    }

    public LayeredWitness(String sigil) {
        super(sigil);
        // this.sigil = sigil;
    }

    public LayeredWitness(String sigil, String content) {
        this(sigil);
        tokenize(content);
    }

    // @Override
    // public String getSigil() {
    // return sigil;
    // }

    @Override
    public Iterator<Token> iterator() {
        return Collections.unmodifiableList(tokens).iterator();
    }

    private void tokenize(String content) {
        WitnessNode wn = WitnessNode.createTree(getSigil(), content);
        tokens = EditGraphAligner.createLabels(wn).stream()//
                .map(this::toToken)//
                .collect(toList());

    }

    private LayerToken toToken(EditGraphTableLabel label) {
        String id = "";
        String data = label.text.data;
        int index = 1;
        int matchIndex = 1;
        String layer = label.layer;
        return new LayerToken(this, id, data, index, matchIndex, layer);
    }
}
