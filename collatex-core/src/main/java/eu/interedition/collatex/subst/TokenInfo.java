package eu.interedition.collatex.subst;

import java.util.Map;

public class TokenInfo {

    private Map<String, String> layerAttributes;
    private String layerName;
    private String sigil;

    public void setSigil(String sigil) {
        this.sigil = sigil;
    }

    public String getSigil() {
        return this.sigil;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public String getLayerName() {
        return this.layerName;
    }

    public boolean inLayer() {
        return this.layerName != null;
    }

    public void setLayerAttributes(Map<String, String> layerAttributes) {
        this.layerAttributes = layerAttributes;
    }

    public Map<String, String> getLayerAttributes() {
        return this.layerAttributes;
    }

}
