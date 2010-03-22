package eu.interedition.collatex2.alignmenttable;

import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.implementation.alignmenttable.Column3;
import eu.interedition.collatex2.implementation.alignmenttable.Superbase4;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
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

  @Test
  public void testCreateSuperBase() {
    final IWitness a = factory.createWitness("A", "the first witness");
    final List<IWitness> set = Lists.newArrayList(a);
    final IAlignmentTable alignmentTable = factory.createNewAlignmentTable(set);
    final ISuperbase superbase = Superbase4.create(alignmentTable);
    Assert.assertEquals("Superbase: (the, first, witness)", superbase.toString());
  }

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
    superbase.addColumn(column);
    Assert.assertEquals("Superbase: (first, second, third)", superbase.toString());
  }

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
    superbase.addColumn(column);
    Assert.assertEquals("Superbase: (first, match)", superbase.toString());
  }

}
