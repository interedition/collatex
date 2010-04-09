package eu.interedition.collatex2.implementation.alignmenttable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableTest {
  @Test
  public void testRepeatingTokensWithOneWitness() {
    final Factory factory = new Factory();
    final IWitness witness = factory.createWitness("a", "a c a t g c a");
    final IAlignmentTable alignmentTable = factory.createAlignmentTable(Lists.newArrayList(witness));
    final List<String> repeatingTokens = alignmentTable.findRepeatingTokens();
    assertEquals(2, repeatingTokens.size());
    assertTrue(repeatingTokens.contains("a"));
    assertTrue(repeatingTokens.contains("c"));
    assertFalse(repeatingTokens.contains("t"));
    assertFalse(repeatingTokens.contains("g"));
  }

  @Ignore
  @Test
  public void testRepeatingTokensWithMultipleWitnesses() {
    final Factory factory = new Factory();
    final IWitness witnessA = factory.createWitness("a", "a c a t g c a");
    final IWitness witnessB = factory.createWitness("b", "a c a t t c a");
    final IAlignmentTable alignmentTable = factory.createAlignmentTable(Lists.newArrayList(witnessA, witnessB));
    final List<String> repeatingTokens = alignmentTable.findRepeatingTokens();
    assertEquals(2, repeatingTokens.size());
    assertTrue(repeatingTokens.contains("a"));
    assertTrue(repeatingTokens.contains("c"));
    assertTrue(repeatingTokens.contains("t"));
    assertFalse(repeatingTokens.contains("g"));
  }

}
