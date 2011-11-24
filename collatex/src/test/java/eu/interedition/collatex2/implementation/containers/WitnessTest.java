package eu.interedition.collatex2.implementation.containers;

import com.google.common.collect.Lists;
import eu.interedition.collatex2.AbstractTest;
import eu.interedition.collatex2.implementation.containers.witness.Witness;
import eu.interedition.collatex2.implementation.input.NormalizedToken;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class WitnessTest extends AbstractTest {

  @Test
  public void isNear() {
    final INormalizedToken token1 = mock(INormalizedToken.class);
    final INormalizedToken token2 = mock(INormalizedToken.class);
    final INormalizedToken token3 = mock(INormalizedToken.class);

    final IWitness w = new Witness("id", Lists.newArrayList(token1, token2, token3));
    assertTrue(w.isNear(token1, token2));
    assertTrue(w.isNear(NormalizedToken.START, token1));
  }

  @Test
  public void isNear2() {
    final IWitness witness = createWitnesses("a b c d e f g h i j k l")[0];
    final Iterator<INormalizedToken> iterator = witness.getTokens().iterator();
    final INormalizedToken a = iterator.next();
    final INormalizedToken b = iterator.next();
    final INormalizedToken c = iterator.next();
    final INormalizedToken d = iterator.next();

    assertTrue(witness.isNear(a, b));
    assertFalse(witness.isNear(a, c));
    assertFalse(witness.isNear(b, d));
    assertTrue(witness.isNear(c, d));
  }

}
