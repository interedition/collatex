package eu.interedition.collatex2.alignmenttable;

import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;

public class AlignmentTableTranspositionTest {
  private static Factory factory;

  @BeforeClass
  public static void setup() {
    factory = new Factory();
  }

  //Note: this is more of an alignment test.. no table is involved here! 
  @Test
  public void testNoTransposition() {
    final IWitness a = factory.createWitness("A", "no transposition");
    final IWitness b = factory.createWitness("B", "no transposition");
    final IAlignment al = factory.createAlignment(a, b);
    Assert.assertTrue(al.getTranspositions().isEmpty());
  }

  //Note: this is more of an alignment test.. no table is involved here! 
  @Test
  public void testDoubleTransposition() {
    final IWitness a = factory.createWitness("A", "a b");
    final IWitness b = factory.createWitness("B", "b a");
    final IAlignment al = factory.createAlignment(a, b);
    Assert.assertEquals(2, al.getTranspositions().size());
    // 1: a -> b
    // 2: b -> a
  }

  @Test
  public void testDoubleTransposition2() {
    final IWitness a = factory.createWitness("A", "a b");
    final IWitness b = factory.createWitness("B", "b a");
    final List<IWitness> set = Lists.newArrayList(a, b);
    final IAlignmentTable alignmentTable = factory.createAlignmentTable(set);
    final String expected = "A:  |a|b\n" + "B: b|a| \n";
    final String actual = alignmentTable.toString();
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testDoubleTransposition3() {
    final IWitness a = factory.createWitness("A", "a b c");
    final IWitness b = factory.createWitness("B", "b a c");
    final List<IWitness> set = Lists.newArrayList(a, b);
    final IAlignmentTable alignmentTable = factory.createAlignmentTable(set);
    final String expected = "A:  |a|b|c\n" + "B: b|a| |c\n";
    final String actual = alignmentTable.toString();
    Assert.assertEquals(expected, actual);
  }

}
