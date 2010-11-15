package eu.interedition.collatex2.experimental.table;
import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;


public class VariantGraphBasedAlignmentTableTest {
    private static CollateXEngine engine;

    @BeforeClass
    public static void setup() {
      engine = new CollateXEngine();
    }

       //NOTE: test taken from AlignmentTableTranspositionTest
    //TODO: rewrite test to work with the new API
    @Ignore
    @Test
    public void testDoubleTransposition2() {
      final IWitness a = engine.createWitness("A", "a b");
      final IWitness b = engine.createWitness("B", "b a");
      final IAlignmentTable alignmentTable = engine.align(a, b);
      final String expected = "A:  |a|b\n" + "B: b|a| \n";
      final String actual = alignmentTable.toString();
      Assert.assertEquals(expected, actual);
    }

}
