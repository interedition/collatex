package eu.interedition.collatex2.alignmenttable;

import java.util.NoSuchElementException;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.implementation.alignmenttable.Column3;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

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
    //    final Witness witnessB = builder.build("B", "different");
    final INormalizedToken word = witness.getTokens().get(0);
    final IColumn column = new Column3(word);
    column.getToken("B");
  }

  //  @Test
  //  public void testContainsWitness() {
  //    final WitnessBuilder builder = new WitnessBuilder();
  //    final Witness witness = builder.build("A", "a test string");
  //    final Witness witnessB = builder.build("B", "different");
  //    final Word word = witness.getFirstSegment().getElementOnWordPosition(1);
  //    final Column column = new Column(word);
  //    assertTrue(column.containsWitness(witness.getFirstSegment()));
  //    assertFalse(column.containsWitness(witnessB.getFirstSegment()));
  //  }
  //
  //  @Test
  //  public void testInverseWordMap() {
  //    final WitnessBuilder builder = new WitnessBuilder();
  //    final Witness witness = builder.build("A", "first");
  //    final Witness witnessB = builder.build("B", "second");
  //    final Witness witnessC = builder.build("C", "third");
  //    final Word word = witness.getFirstSegment().getElementOnWordPosition(1);
  //    final Word wordB = witnessB.getFirstSegment().getElementOnWordPosition(1);
  //    final Word wordC = witnessC.getFirstSegment().getElementOnWordPosition(1);
  //    final Column column = new Column(word);
  //    column.addVariant(wordB);
  //    column.addVariant(wordC);
  //    final Superbase superbase = new Superbase();
  //    column.addToSuperbase(superbase);
  //    assertEquals("first second third", superbase.toString());
  //  }
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

}
