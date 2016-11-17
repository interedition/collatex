package eu.interedition.collatex.subst;

import java.util.ArrayList;
import java.util.List;

public class LayerDefinition {
    private List<OrOperator> orOperators = new ArrayList<>();

    public List<OrOperator> getOrOperators() {
        return this.orOperators;
    }

    public static LayerDefinition getDefault() {
        LayerDefinition LayerDefinition = new LayerDefinition();

        OrOperator substOperator = new OrOperator("subst");
        substOperator.getOrOperands().add("del");
        substOperator.getOrOperands().add("add");
        LayerDefinition.getOrOperators().add(substOperator);

        OrOperator appOperator = new OrOperator("app");
        appOperator.getOrOperands().add("rdg");
        appOperator.setTypeToIgnore("lit");
        LayerDefinition.getOrOperators().add(appOperator);

        return LayerDefinition;
    }

}
