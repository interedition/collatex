package eu.interedition.collatex.experimental.ngrams.transpositions;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import eu.interedition.collatex.experimental.ngrams.Alignment;
import eu.interedition.collatex.experimental.ngrams.BiGramIndex;
import eu.interedition.collatex.experimental.ngrams.BiGramIndexGroup;
import eu.interedition.collatex.experimental.ngrams.NGram;
import eu.interedition.collatex.experimental.ngrams.WitnessSet;
import eu.interedition.collatex.experimental.ngrams.alignment.Gap;
import eu.interedition.collatex.experimental.ngrams.data.Witness;

public class NGramTranspositionTest {

  @Test
  public void testTransposition1() {
    final Witness a = new Witness("A", "The black dog chases a red cat.");
    final Witness b = new Witness("B", "A red cat chases the black dog.");
    final BiGramIndex index = BiGramIndex.create(a);
    Assert.assertEquals(8, index.size());
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    // Bigrams test
    final BiGramIndex uindex = group.getUniqueBigramsForWitnessA();
    //Assert.assertEquals(4, index.size());
    // TODO: I can also make a method that gives back the bigramindex
    // in normalized form as a list of strings
    // NOTE: maybe that method is already there! It is called keys!
    Assert.assertEquals("# the", uindex.get(0).getNormalized());
    Assert.assertEquals("dog chases", uindex.get(1).getNormalized());
    Assert.assertEquals("chases a", uindex.get(2).getNormalized());
    Assert.assertEquals("cat #", uindex.get(3).getNormalized());

    // NGrams test
    final List<NGram> uniqueNGramsForWitnessA = group.getUniqueNGramsForWitnessA();
    Assert.assertEquals(3, uniqueNGramsForWitnessA.size());
    Assert.assertEquals("# the", uniqueNGramsForWitnessA.get(0).getNormalized());
    Assert.assertEquals("dog chases a", uniqueNGramsForWitnessA.get(1).getNormalized());
    Assert.assertEquals("cat #", uniqueNGramsForWitnessA.get(2).getNormalized());
  }

  @Test
  public void testTransposition2Matches() {
    final Witness a = new Witness("A", "The black dog chases a red cat.");
    final Witness b = new Witness("B", "A red cat chases the black dog.");
    final WitnessSet set = new WitnessSet(a, b);
    final Alignment align = set.align();
    final List<NGram> matches = align.getMatches();
    Assert.assertEquals(3, matches.size());
    Assert.assertEquals("the black dog", matches.get(0).getNormalized());
    Assert.assertEquals("chases", matches.get(1).getNormalized());
    Assert.assertEquals("a red cat", matches.get(2).getNormalized());
  }

  @Test
  public void testTransposition3Gaps() {
    final Witness a = new Witness("A", "The black dog chases a red cat.");
    final Witness b = new Witness("B", "A red cat chases the black dog.");
    final WitnessSet set = new WitnessSet(a, b);
    final Alignment align = set.align();
    final List<Gap> gaps = align.getGaps();
    Assert.assertTrue(gaps.toString(), gaps.isEmpty());
  }

}
