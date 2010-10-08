package eu.interedition.collatex.experimental.ngrams;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import eu.interedition.collatex.experimental.interfaces.WitnessF;
import eu.interedition.collatex2.interfaces.IWitness;

public class NGramBasedAlgorithmTest {

  @Test
  public void testNgrams1() {
    // "The black cat", "The black and white cat"
    final IWitness a = WitnessF.create("A", "The black cat");
    final List<BiGram> ngrams = BiGramIndex.calculate(a);
    Assert.assertEquals(4, ngrams.size());
    Assert.assertEquals("# the", ngrams.get(0).getNormalized());
    Assert.assertEquals("the black", ngrams.get(1).getNormalized());
    Assert.assertEquals("black cat", ngrams.get(2).getNormalized());
    Assert.assertEquals("cat #", ngrams.get(3).getNormalized());
  }

  @Test
  public void testNgrams1b() {
    // "The black cat", "The black and white cat"
    final IWitness b = WitnessF.create("B", "The black and white cat");
    final List<BiGram> ngrams = BiGramIndex.calculate(b);
    Assert.assertEquals(6, ngrams.size());
    Assert.assertEquals("# the", ngrams.get(0).getNormalized());
    Assert.assertEquals("the black", ngrams.get(1).getNormalized());
    Assert.assertEquals("black and", ngrams.get(2).getNormalized());
    Assert.assertEquals("and white", ngrams.get(3).getNormalized());
    Assert.assertEquals("white cat", ngrams.get(4).getNormalized());
    Assert.assertEquals("cat #", ngrams.get(5).getNormalized());
  }

  @Test
  public void testOverlappingNGrams2() {
    // "The black cat", "The black and white cat"
    final IWitness a = WitnessF.create("A", "The black cat");
    final IWitness b = WitnessF.create("B", "The black and white cat");
    final List<Subsegment2> overlappingBiGrams = BiGrams.getOverlappingBiGrams(a, b);
    Assert.assertEquals(3, overlappingBiGrams.size());
    Assert.assertEquals("# the A: 0 B: 0", overlappingBiGrams.get(0).toString());
    Assert.assertEquals("the black A: 1 B: 1", overlappingBiGrams.get(1).toString());
    Assert.assertEquals("cat # A: 3 B: 5", overlappingBiGrams.get(2).toString());
  }

  // TODO getUniqueBiGrams should give back an BiGramIndex
  @Test
  public void testOverlappingNGrams2b() {
    // "The black cat", "The black and white cat"
    final IWitness a = WitnessF.create("A", "The black cat");
    final IWitness b = WitnessF.create("B", "The black and white cat");
    final List<BiGram> overlappingBiGrams = BiGrams.getOverlappingBiGramsForWitnessA(a, b);
    Assert.assertEquals(3, overlappingBiGrams.size());
    Assert.assertEquals("# the", overlappingBiGrams.get(0).getNormalized());
    Assert.assertEquals("the black", overlappingBiGrams.get(1).getNormalized());
    Assert.assertEquals("cat #", overlappingBiGrams.get(2).getNormalized());
  }

  @Test
  public void testUniqueNGrams3() {
    // "The black cat", "The black and white cat"
    final IWitness a = WitnessF.create("A", "The black cat");
    final IWitness b = WitnessF.create("B", "The black and white cat");
    final List<NGram> uniqueNGrams = BiGrams.getUniqueBiGramsForWitnessA(a, b);
    Assert.assertEquals(1, uniqueNGrams.size());
    Assert.assertEquals("black cat", uniqueNGrams.get(0).getNormalized());
  }

  @Test
  public void testUniqueNGrams3b() {
    // "The black cat", "The black and white cat"
    final IWitness a = WitnessF.create("A", "The black cat");
    final IWitness b = WitnessF.create("B", "The black and white cat");
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    final List<BiGram> uniqueBiGrams = group.getUniqueBiGramsForWitnessB();
    Assert.assertEquals(3, uniqueBiGrams.size());
    Assert.assertEquals("black and", uniqueBiGrams.get(0).getNormalized());
    Assert.assertEquals("and white", uniqueBiGrams.get(1).getNormalized());
    Assert.assertEquals("white cat", uniqueBiGrams.get(2).getNormalized());
  }

  @Test
  public void testUniqueNGrams3c() {
    // "The black cat", "The black and white cat"
    final IWitness a = WitnessF.create("A", "The black cat");
    final IWitness b = WitnessF.create("B", "The black and white cat");
    final BiGramIndexGroup group = BiGramIndexGroup.create(a, b);
    final List<NGram> uniqueNGrams = group.getUniqueNGramsForWitnessB();
    Assert.assertEquals(1, uniqueNGrams.size());
    Assert.assertEquals("black and white cat", uniqueNGrams.get(0).getNormalized());
  }
}
