package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex.experimental.ngrams.alignment.Alignment;
import eu.interedition.collatex.experimental.ngrams.alignment.Gap;
import eu.interedition.collatex.interfaces.IWitness;
import eu.interedition.collatex.interfaces.WitnessF;

public class NGramAlignmentTest {

  @Test
  public void testAlignment2Gaps() {
    final IWitness a = WitnessF.create("A", "The black cat");
    final IWitness b = WitnessF.create("B", "The black and white cat");
    final Alignment alignment = Alignment.create(a, b);
    final List<Gap> gaps = alignment.getGaps();
    Assert.assertEquals(1, gaps.size());
    final Gap gap = gaps.get(0);
    Assert.assertTrue("NGram A is not empty!", gap.getNGramA().isEmpty());
    Assert.assertEquals("and white", gap.getNGramB().getNormalized());
    Assert.assertTrue(gap.isAddition());
  }

  // Note: taken from TextAlignmentTest!
  @Test
  public void testAddition_AtTheStart() {
    final IWitness a = WitnessF.create("A", "to be");
    final IWitness b = WitnessF.create("B", "not to be");
    final Alignment alignment = Alignment.create(a, b);
    final List<NGram> matches = alignment.getMatches();
    Assert.assertEquals(1, matches.size());
    Assert.assertEquals("to be", matches.get(0).getNormalized());
    final List<Gap> gaps = alignment.getGaps();
    Assert.assertEquals(1, gaps.size());
    Assert.assertTrue(gaps.get(0).isAddition());
    // TODO: add more tests!
    // TODO: if matches become an ngram index the tests could be simpler!
  }

  // TODO: this test should be fixed by enhancing the code to handle this case!
  @Ignore
  @Test
  public void testRepetitionTheBlack() {
    final IWitness a = WitnessF.create("A", "the black cat on the table");
    final IWitness b = WitnessF.create("B", "the black saw on the black cat on the table");
    final Alignment align = Alignment.create(a, b);
    final List<Gap> gaps = align.getGaps();
    Assert.assertEquals(1, gaps.size());
  }

  //A: the black cat on the table
  //B: the black saw the black cat on the table

  //  public void testAlignmentVariant() throws FileNotFoundException, IOException, BlockStructureCascadeException {
  //    final Table table = alignmentTable("cat", "mat");
  //    assertEquals("variant-align: cat / mat", table.get(1, 2).toString());
  //  }
  //
  //  public void testReplacement() throws FileNotFoundException, IOException, BlockStructureCascadeException {
  //    final Table table = alignmentTable("cat", "boat");
  //    assertEquals("replacement: cat / boat", table.get(1, 2).toString());
  //  }
  //
  //  public void testCapital() throws FileNotFoundException, IOException, BlockStructureCascadeException {
  //    final Table table = alignmentTable("the", "The");
  //    assertEquals("identical: the", table.get(1, 2).toString());
  //  }
  //
  //  public void testSentence() throws FileNotFoundException, IOException, BlockStructureCascadeException {
  //    final Table table = alignmentTable("a black cat", "a black cat");
  //    assertEquals("identical: a", table.get(1, 2).toString());
  //    assertEquals("identical: black", table.get(1, 4).toString());
  //    assertEquals("identical: cat", table.get(1, 6).toString());
  //  }

}
