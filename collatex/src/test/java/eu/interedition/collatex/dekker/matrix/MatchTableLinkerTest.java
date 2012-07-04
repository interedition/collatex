package eu.interedition.collatex.dekker.matrix;

import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import com.google.common.collect.Sets;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;

public class MatchTableLinkerTest extends AbstractTest {

  @Test
  //Note: test taken from HermansTest
  public void testHermansText2c() throws XMLStreamException {
    String textD1 = "Op den Atlantischen Oceaan voer een groote stoomer.";
    String textD9 = "Over de Atlantische Oceaan voer een grote stomer.";
    String textDMD1 = "Over de Atlantische Oceaan voer een vreselijk grote stomer.";
    SimpleWitness[] witnesses = createWitnesses(textD1, textD9, textDMD1);

    VariantGraph graph = collate(witnesses[0], witnesses[1]);

    MatchTableLinker linker = new MatchTableLinker();
    Map<Token, VariantGraphVertex> linkedTokens = linker.link(graph, witnesses[2], new EqualityTokenComparator());

    Set<Token> tokens = linkedTokens.keySet();
    Set<String> tokensAsString = Sets.newLinkedHashSet();
    for (Token token : tokens) {
      tokensAsString.add(token.toString());
    }
    System.out.println(tokensAsString);
    assertTrue(tokensAsString.contains("C:0:'over'"));
    assertTrue(tokensAsString.contains("C:1:'de'"));
    assertTrue(tokensAsString.contains("C:2:'atlantische'"));
    assertTrue(tokensAsString.contains("C:3:'oceaan'"));
    assertTrue(tokensAsString.contains("C:4:'voer'"));
    assertTrue(tokensAsString.contains("C:5:'een'"));
    assertTrue(tokensAsString.contains("C:7:'grote'"));
    assertTrue(tokensAsString.contains("C:8:'stomer'"));
  }

  //  String newLine = System.getProperty("line.separator");

  @Test
  public void test1() {
    SimpleWitness[] sw = createWitnesses("A B C A B", "A B C A B");
    VariantGraph vg = collate(sw[0]);
    MatchTableLinker linker = new MatchTableLinker();
    Map<Token, VariantGraphVertex> linkedTokens = linker.link(vg, sw[1], new EqualityTokenComparator());

    Set<Token> tokens = linkedTokens.keySet();
    Set<String> tokensAsString = Sets.newLinkedHashSet();
    for (Token token : tokens) {
      tokensAsString.add(token.toString());
    }
    assertTrue(tokensAsString.contains("B:0:'a'"));
    assertTrue(tokensAsString.contains("B:1:'b'"));
    assertTrue(tokensAsString.contains("B:2:'c'"));
    assertTrue(tokensAsString.contains("B:3:'a'"));
    assertTrue(tokensAsString.contains("B:4:'b'"));
  }
}
