package eu.interedition.collatex.medite;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleVariantGraphSerializer;
import eu.interedition.collatex.util.VariantGraphRanking;
import org.junit.Test;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.PrintWriter;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MediteTest extends AbstractTest {

  @Override
  public void initAlgorithm() {
    collationAlgorithm = CollationAlgorithmFactory.medite(new EqualityTokenComparator());
  }

  @Test
  public void medite() throws XMLStreamException {
    final VariantGraph graph = collate(
            "This Carpenter hadde wedded newe a wyf",
            "This Carpenter hadde wedded a newe wyf",
            "This Carpenter hadde newe wedded a wyf",
            "This Carpenter hadde wedded newly a wyf",
            "This Carpenter hadde E wedded newe a wyf",
            "This Carpenter hadde newli wedded a wyf",
            "This Carpenter hadde wedded a wyf"
    );

    System.out.println(toString(VariantGraphRanking.of(graph).asTable()));

    new SimpleVariantGraphSerializer(graph).toDot(graph, new PrintWriter(System.out));

    final XMLStreamWriter xml = XMLOutputFactory.newFactory().createXMLStreamWriter(System.out);
    new SimpleVariantGraphSerializer(graph).toGraphML(xml);
    xml.close();
  }
}
