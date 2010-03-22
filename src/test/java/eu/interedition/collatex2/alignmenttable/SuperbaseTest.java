package eu.interedition.collatex2.alignmenttable;

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

public class SuperbaseTest {
  private static Factory factory;

  @BeforeClass
  public static void setUp() {
    factory = new Factory();
  }

  //  
  //  @Test
  //  public void testCreateSuperBase() {
  //    final Witness a = builder.build("A", "the first witness");
  //    final AlignmentTable3 alignmentTable = new AlignmentTable3();
  //    alignmentTable.addWitness(a);
  //    final SegmentSuperbase superbase = SegmentSuperbase.create(alignmentTable);
  //    assertEquals("the first witness", superbase.toString());
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

  @Ignore
  @Test
  public void testSuperbase2() {
    final IWitness witness = factory.createWitness("A", "first");
    final IWitness witnessB = factory.createWitness("B", "match");
    final IWitness witnessC = factory.createWitness("C", "match");
    final INormalizedToken word = witness.getTokens().get(0);
    final INormalizedToken wordB = witnessB.getTokens().get(0);
    final INormalizedToken wordC = witnessC.getTokens().get(0);
    final IColumn column = new Column3(word);
    column.addVariant(wordB);
    column.addMatch(wordC);
    final ISuperbase superbase = new Superbase4();
    column.addToSuperbase(superbase);
    Assert.assertEquals("first match", superbase.toString());
  }

}
