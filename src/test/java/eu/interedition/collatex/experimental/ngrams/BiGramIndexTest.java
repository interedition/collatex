package eu.interedition.collatex.experimental.ngrams;

import java.util.Iterator;

import junit.framework.Assert;

import org.junit.Test;

import eu.interedition.collatex.experimental.interfaces.IWitness;
import eu.interedition.collatex.experimental.interfaces.WitnessF;
import eu.interedition.collatex.experimental.ngrams.data.Token;

public class BiGramIndexTest {

  @Test
  public void testCreate() {
    final IWitness a = WitnessF.create("A", "a b c d");
    final BiGramIndex index = BiGramIndex.create(a);
    Assert.assertEquals(5, index.size());
  }

  @Test
  public void testRemoveTokenFromIndex() {
    final IWitness a = WitnessF.create("A", "a b c d");
    final BiGramIndex index = BiGramIndex.create(a);
    final BiGramIndex result = index.removeBiGramsWithToken(new Token("A", "c", 3));
    Assert.assertEquals(3, result.size());
  }

  //  TODO we might want to change this behavior!
  @Test
  public void testIterable() {
    final IWitness a = WitnessF.create("A", "a b c d");
    final BiGramIndex index = BiGramIndex.create(a);
    final Iterator<BiGram> iterator = index.iterator();
    iterator.next();
    iterator.remove();
  }
}
