package eu.interedition.collatex.subst;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * Created by Ronald Haentjens Dekker on 18/08/16.
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
        for (Column c : getTable()) {
            if (c.hasVariation()) {
                renderApp(xwriter, c);
            } else {
                renderLemma(xwriter, c);
            }
        }
        // end root element
        xwriter.writeEndElement();
        xwriter.writeEndDocument();
    }

    private void renderLemma(XMLStreamWriter xwriter, Column column) throws XMLStreamException {
        xwriter.writeCharacters(column.getLemma());
    }

    private void renderApp(XMLStreamWriter xwriter, Column column) throws XMLStreamException {
        xwriter.writeStartElement("app");
        List<String> readings = column.getReadings();
        AtomicInteger varSeqCounter = new AtomicInteger();
        for (String reading : readings) {
            List<String> witnessesForReading = column.getWitnessesForReading(reading);
            if (witnessesForReading.size() == 1) {
                String sigil = witnessesForReading.get(0);
                renderRdg(xwriter, sigil, varSeqCounter, reading);
            } else {
                xwriter.writeStartElement("rdgGrp");
                xwriter.writeAttribute("type", "tag_variation_only");
                for (String sigil : witnessesForReading) {
                    renderRdg(xwriter, sigil, varSeqCounter, reading);
                }
                xwriter.writeEndElement();
            }
        }
        xwriter.writeEndElement();
        xwriter.writeCharacters(" ");
    }

    private void renderRdg(XMLStreamWriter xwriter, String sigil, AtomicInteger varSeqCounter, String reading) throws XMLStreamException {
        String baseSigil = sigil.replaceAll("-subst.*", "");
        xwriter.writeStartElement("rdg");
        xwriter.writeAttribute("wit", "#" + baseSigil);
        if (!sigil.equals(baseSigil)) {
            String layer = sigil.split("-")[2];
            xwriter.writeAttribute("varSeq", String.valueOf(varSeqCounter.getAndIncrement()));
            xwriter.writeStartElement(layer);
            xwriter.writeCharacters(reading);
            xwriter.writeEndElement();

        } else {
            xwriter.writeCharacters(reading);
        }
        xwriter.writeEndElement();
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
            // NOTE: key set is a linked set!
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
        return createColumns(witnessLabels, rankToWitnessNodes);
    }

    private List<Column> createColumns(Map<WitnessNode, String> witnessLabels, Map<Integer, List<List<WitnessNode>>> inverse) {
        List<Column> columns = new ArrayList<>();
        for (int i = 0; i < inverse.keySet().size(); i++) {
            List<List<WitnessNode>> matches = inverse.get(i);
            // in the most simple case we treat all the witness nodes separately
            LinkedHashMap<String, String> labelToNode = new LinkedHashMap<>();
            matches.stream()//
                    .flatMap(List::stream)//
                    .forEach(node -> {
                        // Only normalize when there is variation in a column.
                        String value = node.data;
                        if (matches.size() > 1) {
                            // TODO: MOVE NORMALIZATION TO A DIFFERENT PLACE!
                            value = value.trim();
                        }
                        labelToNode.put(witnessLabels.get(node), value);
                    });
            // TODO: the inverseMap method has no guaranteed order
            // TODO: unit tests should fail, but don't at this time!
            Map<String, List<String>> readingToLayerIdentifiers = inverseMap(labelToNode);
            Column column = new Column(readingToLayerIdentifiers);
            columns.add(column);
        }
        return columns;
    }

    private static <T, U> Map<T, List<U>> inverseMap(Map<U, T> original) {
        Map<T, List<U>> inverse = new HashMap<>();
        Set<Map.Entry<U, T>> entries = original.entrySet();
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
        superWitness.stream().flatMap(List::stream).forEach(n -> {
            String label = n.getSigil();
            StringBuilder x = new StringBuilder();
            n.parentNodeStream().forEach(p -> {
                if (!p.data.equals("wit") && !p.data.equals("fake root")) {
                    x.insert(0, "-" + p.data);
                }
            });
            label += x.toString();
            result.put(n, label);
        });
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
        index.forEach(i -> ranks.put(superWitness.get(i), ranksAsRange.get(i)));
        return ranks;
    }
}
