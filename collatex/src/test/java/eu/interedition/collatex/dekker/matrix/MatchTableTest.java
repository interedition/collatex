package eu.interedition.collatex.dekker.matrix;

import org.junit.Test;

import com.google.common.collect.ArrayTable;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.simple.SimpleWitness;

public class MatchTableTest extends AbstractTest {

  @Test
  public void testTableCreationAbAcAbc() {
    SimpleWitness[] witnesses = createWitnesses("a b", "a c", "a b c");
    VariantGraph graph = collate(witnesses[0], witnesses[1]);
    MatchTable matrix = MatchTable.create(graph, witnesses[2]);
    ArrayTable<Token, Integer, VariantGraphVertex> table = matrix.getTable();
    assertVertexEquals("a", table.at(0, 0));
    assertVertexEquals("b", table.at(1, 1));
    assertVertexEquals("c", table.at(2, 1));
  }
  
  @Test
  public void testTableCreationAbcabCab() {
    SimpleWitness[] witnesses = createWitnesses("a b c a b", "c a b");
    VariantGraph graph = collate(witnesses[0]);
    MatchTable matrix = MatchTable.create(graph, witnesses[1]);
    ArrayTable<Token, Integer, VariantGraphVertex> table = matrix.getTable();
    assertVertexEquals("a", table.at(1, 0));
    assertVertexEquals("b", table.at(2, 1));
    assertVertexEquals("c", table.at(0, 2));
    assertVertexEquals("a", table.at(1, 3));
    assertVertexEquals("b", table.at(2, 4));
  }

}
