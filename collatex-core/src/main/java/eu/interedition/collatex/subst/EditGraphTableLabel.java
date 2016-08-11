package eu.interedition.collatex.subst;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class EditGraphTableLabel {
    static final String SUBST = "subst";
    String layer;
    List<WitnessNode> startElements = new ArrayList<>();
    List<WitnessNode> endElements = new ArrayList<>();
    WitnessNode text;

    public void addStartEvent(WitnessNode node) {
        this.startElements.add(node);
    }

    public void addEndEvent(WitnessNode node) {
        this.endElements.add(node);
    }

    public void addTextEvent(WitnessNode node) {
        if (this.text != null) {
            throw new UnsupportedOperationException();
        }
        this.text = node;
    }

    @Override
    public String toString() {
        String a = this.startElements.stream().map(WitnessNode::toString).collect(Collectors.joining(", "));
        String b = this.text.toString();
        String c = this.endElements.stream().map(WitnessNode::toString).collect(Collectors.joining(", "));
        return MessageFormat.format("{0}:{1}:{2}", b, a, c);
    }

    public boolean containsStartSubstOption() {
        // find the first add or del tag...
        // Note that this implementation is from the left to right
        return containsSubstOption(this.startElements);
    }

    public boolean containsEndSubstOption() {
        // find the first add or del tag...
        // Note that this implementation is from the left to right
        return containsSubstOption(this.endElements);
    }

    private boolean containsSubstOption(List<WitnessNode> witnessNodeList) {
        return witnessNodeList.stream()//
                .filter(this::isSubstOption)//
                .findFirst()//
                .isPresent();
    }

    public boolean containsStartSubst() {
        return containsSubst(this.startElements);
    }

    private boolean containsSubst(List<WitnessNode> witnessNodeList) {
        return witnessNodeList.stream()//
                .filter(this::isSubst)//
                .findFirst()//
                .isPresent();
    }

    public boolean containsEndSubst() {
        // find the first end subst tag...
        // if we want to do this completely correct it should be from right to left
        return containsSubst(this.endElements);
    }

    public WitnessNode getEndSubstNodes() {
        return this.endElements.stream()//
                .filter(this::isSubst)//
                .findFirst().orElseThrow(() -> new RuntimeException("We expected one or more subst tags here!"));
    }

    private Boolean isSubst(WitnessNode node) {
        return node.isElement()//
                && SUBST.equals(node.data);
    }

    private Boolean isSubstOption(WitnessNode node) {
        return node.isElement()//
                && (node.data.equals("add") || node.data.equals("del"));
    }
}
