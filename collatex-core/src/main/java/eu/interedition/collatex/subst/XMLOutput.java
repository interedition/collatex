package eu.interedition.collatex.subst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ronalddekker on 18/08/16.
 */
public class XMLOutput {
    private final ArrayList<XMLOutput.Column> table;
    private final List<List<WitnessNode>> superWitness;

    // We now have the matches, witness labels and ranks
    // Ik wil eigenlijk een type opslaan op de column
    // er kan variatie zijn of niet
    public class Column {
        private final Map<String, WitnessNode> rows;

        public Column() {
            this.rows = new HashMap<>();
        }

        public boolean hasVariation() {
            return false;
        }

        public String getReading() {
            return this.rows.get("lemma").data;
        }

        public String getReading(String witness) {
            return this.rows.get(witness).data;
        }
    }

    public XMLOutput(List<List<WitnessNode>> superWitness) {
        // To create the table we need the layer identifiers as witness identifiers (NB: not per witness, but per layer!)
        // Then we need the ranks per element of the superwitness (element -> in other words: groups of tokens that match)
        this.table = new ArrayList<>();
        this.superWitness = superWitness;
    }

    public List<Column> getTable() {
        return table;
    }

    public Map<WitnessNode, String> getWitnessLabels() {
        Map<WitnessNode, String> result = new HashMap<>();
        superWitness.stream().forEach(l -> l.stream().forEach(n -> {
                String label = n.getSigil();
                StringBuilder x = new StringBuilder();
                n.parentNodeStream().forEach(p -> {
                    if (p.data != "wit" && p.data != "fake root") {
                        x.insert(0, "-"+p.data);
                    }
                });
                label += x.toString();
                result.put(n, label);
            }
        ));
        return result;
    }


}
