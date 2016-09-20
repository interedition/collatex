package eu.interedition.collatex.subst;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;

public class LayerToken implements Token, Comparable<LayerToken> {
    private String id;
    private String data;
    private Boolean hasMatch;
    private int index;
    private int matchIndex;

    private Witness witness;
    private String layer;

    public LayerToken(String sigil, String id, String data, int index, int matchIndex, String layer) {
        this.id = id;
        this.data = data;
        this.index = index;
        this.matchIndex = matchIndex;
        this.layer = layer;
        this.hasMatch = matchIndex > 0;
        this.witness = new LayeredWitness(sigil);
    }

    public String getId() {
        return this.id;
    }

    public int getIndex() {
        return this.index;
    }

    public String getData() {
        return this.data;
    }

    public Boolean hasMatch() {
        return this.hasMatch;
    }

    public int getMatchIndex() {
        return this.matchIndex;
    }

    @Override
    public Witness getWitness() {
        return this.witness;
    }

    @Override
    public int compareTo(LayerToken other) {
        return getId().compareTo(other.getId());
    }

    public String getLayer() {
        return this.layer;
    }
}