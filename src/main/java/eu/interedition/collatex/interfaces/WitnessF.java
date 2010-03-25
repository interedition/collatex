package eu.interedition.collatex.interfaces;

import eu.interedition.collatex.general.NormalizedWitnessBuilder;

public class WitnessF {
  public static IWitness create(final String sigil, final String words) {
    return NormalizedWitnessBuilder.create(sigil, words);
  }
}
