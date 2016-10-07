package eu.interedition.collatex.subst;

import java.util.ArrayList;
import java.util.List;

public class OrOperator {
    String name;
    List<String> orOperands = new ArrayList<>();
    String typeToIgnore;

    public OrOperator(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getOrOperands() {
        return this.orOperands;
    }

    public String getTypeToIgnore() {
        return this.typeToIgnore;
    }

    public void setTypeToIgnore(String typeToIgnore) {
        this.typeToIgnore = typeToIgnore;
    }
}
