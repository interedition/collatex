package eu.interedition.collatex2.implementation.containers.witness;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import eu.interedition.collatex2.implementation.input.NormalizedToken;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class WitnessTest {
  @Test
  public void testIsNear() {
    INormalizedToken token1 = mock(INormalizedToken.class);
    INormalizedToken token2 = mock(INormalizedToken.class);
    INormalizedToken token3 = mock(INormalizedToken.class);
    List<INormalizedToken> tokens = Lists.newArrayList();
    tokens.add(token1);
    tokens.add(token2);
    tokens.add(token3);
    IWitness w = new Witness("id", tokens);
    assertTrue(w.isNear(token1, token2));
    assertTrue(w.isNear(NormalizedToken.START, token1));
  }
}
