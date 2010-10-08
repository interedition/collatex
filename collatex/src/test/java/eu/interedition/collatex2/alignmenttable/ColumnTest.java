package eu.interedition.collatex2.alignmenttable;

import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.ColumnState;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.legacy.alignmenttable.Column3;

//NOTE: this is an implementation test!
//NOTE: constructors are called directly!
//NOTE: Not only read only methods are called!
public class ColumnTest {
  private static CollateXEngine factory;

  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }

  @Test(expected = NoSuchElementException.class)
  public void testGetWordNonExistingGivesException() {
    final IWitness witness = factory.createWitness("A", "a test string");
    final INormalizedToken word = witness.getTokens().get(0);
    final IColumn column = new Column3(word, 1);
    column.getToken("B");
  }

  @Test
  public void testFirstToken() {
    final IWitness witness = factory.createWitness("A", "a test string");
    final INormalizedToken word = witness.getTokens().get(0);
    final IColumn column = new Column3(word, 1);
    Assert.assertTrue(column.containsWitness("A"));
    Assert.assertFalse(column.containsWitness("B"));
    Assert.assertEquals(ColumnState.NEW, column.getState());
  }

  @Test
  public void testAddVariant() {
    final IWitness witness = factory.createWitness("A", "first");
    final IWitness witnessB = factory.createWitness("B", "second");
    final IWitness witnessC = factory.createWitness("C", "third");
    final INormalizedToken word = witness.getTokens().get(0);
    final INormalizedToken wordB = witnessB.getTokens().get(0);
    final INormalizedToken wordC = witnessC.getTokens().get(0);
    final IColumn column = new Column3(word, 1);
    column.addVariant(wordB);
    column.addVariant(wordC);
    final List<INormalizedToken> variants = column.getVariants();
    Assert.assertEquals(3, variants.size());
    Assert.assertEquals("first", variants.get(0).getNormalized());
    Assert.assertEquals("second", variants.get(1).getNormalized());
    Assert.assertEquals("third", variants.get(2).getNormalized());
    Assert.assertTrue(column.containsWitness("A"));
    Assert.assertTrue(column.containsWitness("B"));
    Assert.assertTrue(column.containsWitness("C"));
    Assert.assertFalse(column.containsWitness("D"));
    Assert.assertEquals(ColumnState.VARIANT, column.getState());
  }

  @Test
  public void testAddMatch() {
    final IWitness a = factory.createWitness("A", "match");
    final IWitness b = factory.createWitness("B", "match");
    final INormalizedToken wordA = a.getTokens().get(0);
    final INormalizedToken wordB = b.getTokens().get(0);
    final IColumn column = new Column3(wordA, 1);
    column.addMatch(wordB);
    final List<INormalizedToken> variants = column.getVariants();
    Assert.assertEquals(1, variants.size());
    Assert.assertEquals("match", variants.get(0).getNormalized());
    Assert.assertTrue(column.containsWitness("A"));
    Assert.assertTrue(column.containsWitness("B"));
    Assert.assertFalse(column.containsWitness("C"));
    Assert.assertEquals(ColumnState.MATCH, column.getState());
  }

  @Test
  public void testMixedColumn() {
    final IWitness witness = factory.createWitness("A", "match");
    final IWitness witnessB = factory.createWitness("B", "match");
    final IWitness witnessC = factory.createWitness("C", "variant");
    final INormalizedToken word = witness.getTokens().get(0);
    final INormalizedToken wordB = witnessB.getTokens().get(0);
    final INormalizedToken wordC = witnessC.getTokens().get(0);
    final IColumn column = new Column3(word, 1);
    column.addMatch(wordB);
    column.addVariant(wordC);
    final List<INormalizedToken> variants = column.getVariants();
    Assert.assertEquals(2, variants.size());
    Assert.assertEquals("match", variants.get(0).getNormalized());
    Assert.assertEquals("variant", variants.get(1).getNormalized());
    Assert.assertTrue(column.containsWitness("A"));
    Assert.assertTrue(column.containsWitness("B"));
    Assert.assertTrue(column.containsWitness("C"));
    Assert.assertFalse(column.containsWitness("D"));
    Assert.assertEquals(ColumnState.VARIANT, column.getState());
  }
}
