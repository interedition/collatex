package eu.interedition.collatex.dekker.matrix;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.jung.JungVariantGraph;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;

public class MatchTableTest extends AbstractTest {

  // helper method
  private void assertIslandEquals(int leftRow, int leftColumn, int rightRow, int rightColumn, Island island) {
    Coordinate leftEnd = island.getLeftEnd();
    assertEquals(leftRow, leftEnd.getRow());
    assertEquals(leftColumn, leftEnd.getColumn());
    Coordinate rightEnd = island.getRightEnd();
    assertEquals(rightRow, rightEnd.getRow());
    assertEquals(rightColumn, rightEnd.getColumn());
  }

  @Test
  public void testTableCreationEmptyGraph() {
    final VariantGraph graph = new JungVariantGraph();
    SimpleWitness[] witnesses = createWitnesses("a b");
    MatchTable table = MatchTable.create(graph, witnesses[0]);
    assertEquals(1, table.columnList().size());
  }

  @Test
  public void testTableCreationVariationDoesNotCauseExtraColumns() {
    SimpleWitness[] witnesses = createWitnesses("a", "b", "c", "d");
    VariantGraph graph = collate(witnesses[0], witnesses[1], witnesses[2]);
    MatchTable table = MatchTable.create(graph, witnesses[3]);
    assertEquals(1, table.columnList().size());
  }

  @Test
  public void testTableCreationAbAcAbc() {
    SimpleWitness[] witnesses = createWitnesses("a b", "a c", "a b c");
    VariantGraph graph = collate(witnesses[0], witnesses[1]);
    MatchTable table = MatchTable.create(graph, witnesses[2]);
    assertVertexEquals("a", table.vertexAt(0, 0));
    assertVertexEquals("b", table.vertexAt(1, 1));
    assertVertexEquals("c", table.vertexAt(2, 1));
  }

  @Test
  public void testTableCreationAbcabCab() {
    SimpleWitness[] witnesses = createWitnesses("a b c a b", "c a b");
    VariantGraph graph = collate(witnesses[0]);
    MatchTable table = MatchTable.create(graph, witnesses[1]);
    assertVertexEquals("a", table.vertexAt(1, 0));
    assertVertexEquals("b", table.vertexAt(2, 1));
    assertVertexEquals("c", table.vertexAt(0, 2));
    assertVertexEquals("a", table.vertexAt(1, 3));
    assertVertexEquals("b", table.vertexAt(2, 4));
  }

  @Test
  public void testTableCreationAbcabAbcab() {
    SimpleWitness[] sw = createWitnesses("A B C A B", "A B C A B");
    VariantGraph vg = collate(sw[0]);
    MatchTable table = MatchTable.create(vg, sw[1], new EqualityTokenComparator());
    assertEquals(5, table.columnList().size());
    assertEquals(5, table.rowList().size());
    assertVertexEquals("a", table.vertexAt(0, 0));
    assertVertexEquals("a", table.vertexAt(0, 3));
    assertVertexEquals("b", table.vertexAt(1, 1));
    assertVertexEquals("b", table.vertexAt(1, 4));
    assertVertexEquals("c", table.vertexAt(2, 2));
    assertVertexEquals("a", table.vertexAt(3, 0));
    assertVertexEquals("a", table.vertexAt(3, 3));
    assertVertexEquals("b", table.vertexAt(4, 1));
    assertVertexEquals("b", table.vertexAt(4, 4));
  }

  @Test
  public void testTableCreationAsymmatricMatrix() {
    SimpleWitness[] sw = createWitnesses("A B A B C", "A B C A B");
    VariantGraph vg = collate(sw[0]);
    MatchTable table = MatchTable.create(vg, sw[1], new EqualityTokenComparator());
    assertVertexEquals("a", table.vertexAt(0, 0));
    assertVertexEquals("a", table.vertexAt(0, 2));
    assertVertexEquals("b", table.vertexAt(1, 1));
    assertVertexEquals("b", table.vertexAt(1, 3));
    assertVertexEquals("c", table.vertexAt(2, 4));
    assertVertexEquals("a", table.vertexAt(3, 0));
    assertVertexEquals("a", table.vertexAt(3, 2));
    assertVertexEquals("b", table.vertexAt(4, 1));
    assertVertexEquals("b", table.vertexAt(4, 3));
  }

  @Test
  public void testRowLabels() {
    String textD1 = "de het een";
    String textD9 = "het een de";
    SimpleWitness[] sw = createWitnesses(textD1, textD9);
    VariantGraph vg = collate(sw[0]);
    MatchTable table = MatchTable.create(vg, sw[1], new EqualityTokenComparator());
    List<Token> labels = table.rowList();
    assertTokenEquals("het ", labels.get(0));
    assertTokenEquals("een ", labels.get(1));
    assertTokenEquals("de", labels.get(2));
  }

  @Test
  public void testColumnLabels() {
    String textD1 = "de het een";
    String textD9 = "het een de";
    SimpleWitness[] sw = createWitnesses(textD1, textD9);
    VariantGraph vg = collate(sw[0]);
    MatchTable table = MatchTable.create(vg, sw[1], new EqualityTokenComparator());
    List<Integer> labels = table.columnList();
    assertEquals((Integer) 0, labels.get(0));
    assertEquals((Integer) 1, labels.get(1));
    assertEquals((Integer) 2, labels.get(2));
  }

  @Test
  public void testGetAllMatches() {
    SimpleWitness[] sw = createWitnesses("A B A B C", "A B C A B");
    VariantGraph vg = collate(sw[0]);
    MatchTable table = MatchTable.create(vg, sw[1], new EqualityTokenComparator());
    List<Coordinate> allTrue = table.allMatches();
    assertEquals(9, allTrue.size());
    assertTrue(allTrue.contains(new Coordinate(0, 0)));
    assertFalse(allTrue.contains(new Coordinate(0, 1)));
  }

  @Test
  public void testIslandDetectionAbcabCab() {
    SimpleWitness[] witnesses = createWitnesses("a b c a b", "c a b");
    VariantGraph graph = collate(witnesses[0]);
    MatchTable table = MatchTable.create(graph, witnesses[1]);
    List<Island> islands = Lists.newArrayList(table.getIslands());
    assertEquals(2, islands.size());
    Collections.sort(islands);
    Island island = islands.get(1);
    assertIslandEquals(0, 2, 2, 4, island);
  }

  @Test
  public void testIslandDetectionXabcabXcab() {
    SimpleWitness[] witnesses = createWitnesses("x a b c a b", "x c a b");
    VariantGraph graph = collate(witnesses[0]);
    MatchTable table = MatchTable.create(graph, witnesses[1]);
    List<Island> islands = Lists.newArrayList(table.getIslands());
    assertEquals(3, islands.size());
    Collections.sort(islands);
    Island island = islands.get(0);
    assertIslandEquals(0, 0, 0, 0, island);
  }

}
