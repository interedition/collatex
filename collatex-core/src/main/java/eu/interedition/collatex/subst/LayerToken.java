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

    public LayerToken(Witness witness, String id, String data, int index, int matchIndex, String layer) {
        this.id = id;
        this.data = data;
        this.index = index;
        this.matchIndex = matchIndex;
        this.layer = layer;
        this.hasMatch = matchIndex > 0;
        this.witness = witness;
    }

    public String getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public String getData() {
        return data;
    }

    public Boolean hasMatch() {
        return hasMatch;
    }

    public int getMatchIndex() {
        return matchIndex;
    }

    public String getLayer() {
        return layer;
    }

    public Integer matchIndex() {
        return matchIndex;
    }

    @Override
    public Witness getWitness() {
        return witness;
    }

    @Override
    public int compareTo(LayerToken other) {
        return getId().compareTo(other.getId());
    }

}
