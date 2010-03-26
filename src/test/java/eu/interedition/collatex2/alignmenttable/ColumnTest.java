package eu.interedition.collatex2.alignmenttable;

import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.implementation.alignmenttable.Column3;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

//TODO: Add tests for the state of the Column
//NOTE: this is an implementation test!
//NOTE: constructors are called directly!
//NOTE: Not only read only methods are called!
public class ColumnTest {
  private static Factory factory;

  @BeforeClass
  public static void setup() {
    factory = new Factory();
  }

  @Test(expected = NoSuchElementException.class)
  public void testGetWordNonExistingGivesException() {
    final IWitness witness = factory.createWitness("A", "a test string");
    final INormalizedToken word = witness.getTokens().get(0);
    final IColumn column = new Column3(word);
    column.getToken("B");
  }

  @Test
  public void testFirstToken() {
    final IWitness witness = factory.createWitness("A", "a test string");
    final INormalizedToken word = witness.getTokens().get(0);
    final IColumn column = new Column3(word);
    Assert.assertTrue(column.containsWitness("A"));
    Assert.assertFalse(column.containsWitness("B"));
  }

  @Test
  public void testAddVariant() {
    final IWitness witness = factory.createWitness("A", "first");
    final IWitness witnessB = factory.createWitness("B", "second");
    final IWitness witnessC = factory.createWitness("C", "third");
    final INormalizedToken word = witness.getTokens().get(0);
    final INormalizedToken wordB = witnessB.getTokens().get(0);
    final INormalizedToken wordC = witnessC.getTokens().get(0);
    final IColumn column = new Column3(word);
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
  }

  @Test
  public void testAddMatch() {
    final IWitness witness = factory.createWitness("A", "first");
    final IWitness witnessB = factory.createWitness("B", "match");
    final IWitness witnessC = factory.createWitness("C", "match");
    final INormalizedToken word = witness.getTokens().get(0);
    final INormalizedToken wordB = witnessB.getTokens().get(0);
    final INormalizedToken wordC = witnessC.getTokens().get(0);
    final IColumn column = new Column3(word);
    column.addVariant(wordB);
    column.addMatch(wordC);
    final List<INormalizedToken> variants = column.getVariants();
    Assert.assertEquals(2, variants.size());
    Assert.assertEquals("first", variants.get(0).getNormalized());
    Assert.assertEquals("match", variants.get(1).getNormalized());
    Assert.assertTrue(column.containsWitness("A"));
    Assert.assertTrue(column.containsWitness("B"));
    Assert.assertTrue(column.containsWitness("C"));
  }

}
