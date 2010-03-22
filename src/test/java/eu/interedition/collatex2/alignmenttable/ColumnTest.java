package eu.interedition.collatex2.alignmenttable;

import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.implementation.alignmenttable.Column3;
import eu.interedition.collatex2.implementation.alignmenttable.Superbase4;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ISuperbase;
import eu.interedition.collatex2.interfaces.IWitness;

//TODO: move superbase part to another test!
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
  public void testContainsWitness() {
    final IWitness witness = factory.createWitness("A", "a test string");
    final INormalizedToken word = witness.getTokens().get(0);
    final IColumn column = new Column3(word);
    Assert.assertTrue(column.containsWitness("A"));
    Assert.assertFalse(column.containsWitness("B"));
  }

  //TODO: Rename test?`
  //TODO: This test tests addVariant 
  @Test
  public void testInverseWordMap() {
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
  }

  //
  //  @Test
  //  public void testInverseWordMap2() {
  //    final WitnessBuilder builder = new WitnessBuilder();
  //    final Witness witness = builder.build("A", "first");
  //    final Witness witnessB = builder.build("B", "match");
  //    final Witness witnessC = builder.build("C", "match");
  //    final Word word = witness.getFirstSegment().getElementOnWordPosition(1);
  //    final Word wordB = witnessB.getFirstSegment().getElementOnWordPosition(1);
  //    final Word wordC = witnessC.getFirstSegment().getElementOnWordPosition(1);
  //    final Column column = new Column(word);
  //    column.addVariant(wordB);
  //    column.addMatch(wordC);
  //    final Superbase superbase = new Superbase();
  //    column.addToSuperbase(superbase);
  //    assertEquals("first match", superbase.toString());
  //  }

  @Ignore
  @Test
  public void testSuperbase1() {
    final IWitness witness = factory.createWitness("A", "first");
    final IWitness witnessB = factory.createWitness("B", "second");
    final IWitness witnessC = factory.createWitness("C", "third");
    final INormalizedToken word = witness.getTokens().get(0);
    final INormalizedToken wordB = witnessB.getTokens().get(0);
    final INormalizedToken wordC = witnessC.getTokens().get(0);
    final IColumn column = new Column3(word);
    column.addVariant(wordB);
    column.addVariant(wordC);
    final ISuperbase superbase = new Superbase4();
    column.addToSuperbase(superbase);
    Assert.assertEquals("first second third", superbase.toString());
  }

}
