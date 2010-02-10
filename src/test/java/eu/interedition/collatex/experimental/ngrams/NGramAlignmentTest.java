package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import eu.interedition.collatex.experimental.ngrams.data.Witness;

public class NGramAlignmentTest {

  //Copied from TextAlignmentTest
  // TODO: remove # .. # !
  @Test
  public void testAlignment() {
    final Witness a = new Witness("A", "cat");
    final Witness b = new Witness("B", "cat");
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    final Alignment alignment = group.align();
    final List<NGram> matches = alignment.getMatches();
    Assert.assertEquals(1, matches.size());
    Assert.assertEquals("# cat #", matches.get(0).getNormalized());
  }

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
