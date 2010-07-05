package eu.interedition.collatex.experimental.interfaces;

import eu.interedition.collatex.general.NormalizedWitnessBuilder;
import eu.interedition.collatex2.interfaces.IWitness;

public class WitnessF {
  public static IWitness create(final String sigil, final String words) {
    return NormalizedWitnessBuilder.create(sigil, words);
  }
}
