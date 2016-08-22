package eu.interedition.collatex.subst;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Created by ronalddekker on 18/08/16.
 */
public class XMLOutput {
    private final List<List<WitnessNode>> superWitness;

    public void printXML(Writer writer) throws XMLStreamException {
        XMLOutputFactory output = XMLOutputFactory.newInstance();
        XMLStreamWriter xwriter = output.createXMLStreamWriter(writer);
        xwriter.writeStartDocument();
        // add root element
        xwriter.writeStartElement("apparatus");
        // here we have to go over the columns
        // TODO: hardcoded to get data from first column
        Column c1 = getTable().get(0);
        xwriter.writeCharacters(c1.getLemma());
        xwriter.writeEndElement();
        xwriter.writeEndDocument();
    }

    // We now have the matches, witness labels and ranks
    // A column can contain variation or not
    public class Column {
        private final Map<String, List<String>> readingToLayerIdentifiers;

        public Column(Map<String, List<String>> readingToLayerIdentifiers) {
            this.readingToLayerIdentifiers = readingToLayerIdentifiers;
        }

        public boolean hasVariation() {
            return readingToLayerIdentifiers.size() != 1;
        }

        public String getLemma() {
            return readingToLayerIdentifiers.keySet().iterator().next();
        }

        public List<String> getWitnessesForReading(String reading) {
            return readingToLayerIdentifiers.get(reading);
        }

        public List<String> getReadings() {
            //NOTE: key set is a linked set!
            return new ArrayList<>(readingToLayerIdentifiers.keySet());
        }
    }

    public XMLOutput(List<List<WitnessNode>> superWitness) {
        // To create the table we need the layer identifiers as witness identifiers (NB: not per witness, but per layer!)
        // Then we need the ranks per element of the superwitness (element -> in other words: groups of tokens that match)
        this.superWitness = superWitness;
    }

    public List<Column> getTable() {
        // To create the table we need to label each of the layers in the witnesses.
        Map<WitnessNode, String> witnessLabels = getWitnessLabels();
        // We need to rank all the matches / non-matches
        Map<List<WitnessNode>, Integer> ranksForMatchesAndNonMatches = getRanksForMatchesAndNonMatches();
        Map<Integer, List<List<WitnessNode>>> rankToWitnessNodes = inverseMap(ranksForMatchesAndNonMatches);
        // Given the ranks and the labels we create the columns
        List<Column> columns = createColumns(witnessLabels, rankToWitnessNodes);
        return columns;
    }

    private List<Column> createColumns(Map<WitnessNode, String> witnessLabels, Map<Integer, List<List<WitnessNode>>> inverse) {
        List<Column> columns = new ArrayList<>();
        for (int i=0; i < inverse.keySet().size(); i++) {
            List<List<WitnessNode>> matches = inverse.get(i);
            //TODO: matches.stream().map(TODO)
            // in the most simple case we treat all the witness nodes separately
            LinkedHashMap<String, String> labelToNode = new LinkedHashMap<>();
            for (List<WitnessNode> match : matches) {
                for (WitnessNode node : match) {
                    // Only normalize when there is variation in a column.
                    String value = node.data;
                    if (matches.size()>1) {
                        // TODO: MOVE NORMALIZATION TO A DIFFERENT PLACE!
                        value = value.trim();
                    }
                    labelToNode.put(witnessLabels.get(node), value);
               }
            }
            //TODO: the inverseMap method has no garanteed order
            //TODO: unit tests should fail, but don't at this time!
            Map<String, List<String>> readingToLayerIdentifiers = inverseMap(labelToNode);
            Column column = new Column(readingToLayerIdentifiers);
            columns.add(column);
        }
        return columns;
    }

    private static <T, U> Map<T, List<U>> inverseMap(Map<U, T> ranksForMatchesAndNonMatches) {
        Map<T, List<U>> inverse = new HashMap<>();
        Set<Map.Entry<U, T>> entries = ranksForMatchesAndNonMatches.entrySet();
        for (Map.Entry<U, T> entry : entries) {
            if (inverse.containsKey(entry.getValue())) {
                inverse.get(entry.getValue()).add(entry.getKey());
            } else {
                inverse.put(entry.getValue(), new ArrayList<>(Collections.singleton(entry.getKey())));
            }
        }
        return inverse;
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

    // TODO: this implementation is too rigid!
    public Map<List<WitnessNode>, Integer> getRanksForMatchesAndNonMatches() {
        // TODO: hardcoded!
        List<Integer> ranksAsRange = Arrays.asList(0, 1, 1, 2, 3);
        // Java 8 has no stream with counter, nor a zip function... sigh
        // We walk over the index using a range on an int stream ...
        IntStream index = IntStream.range(0, ranksAsRange.size());
        Map<List<WitnessNode>, Integer> ranks = new HashMap<>();
        index.forEach( i -> {
            ranks.put(superWitness.get(i), ranksAsRange.get(i));
        });
        return ranks;
    }




}
