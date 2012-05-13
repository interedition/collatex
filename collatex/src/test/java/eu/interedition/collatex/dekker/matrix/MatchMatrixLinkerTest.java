package eu.interedition.collatex.dekker.matrix;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;

public class MatchMatrixLinkerTest extends AbstractTest {

  //  String newLine = System.getProperty("line.separator");

  @Test
  public void test1() {
    SimpleWitness[] sw = createWitnesses("A B C A B", "A B C A B");
    int baseWitness = 0;
    int posBaseWitness = 0;
    int secondWitness = 1;
    int posSecondWitness = 0;

    VariantGraph vg = collate(sw[posBaseWitness]);
    MatchMatrixLinker linker = new MatchMatrixLinker();
    Map<Token, VariantGraphVertex> link = linker.link(vg, sw[1], new EqualityTokenComparator());

    compareWitnesses(sw, baseWitness, posBaseWitness, secondWitness, posSecondWitness, link);
  }

  private void compareWitnesses(SimpleWitness[] sw, int baseWitness, int posBaseWitness, int secondWitness, int posSecondWitness, Map<Token, VariantGraphVertex> link) {
    List<Token> lt = sw[secondWitness].getTokens();
    VariantGraphVertex variantGraphVertex = link.get(lt.get(posSecondWitness));

    Set<Token> tokens = variantGraphVertex.tokens();
    Token next = tokens.iterator().next();
    assertEquals(sw[baseWitness].getTokens().get(posBaseWitness), next);
  }

}
