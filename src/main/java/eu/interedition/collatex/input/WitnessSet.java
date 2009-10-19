package eu.interedition.collatex.input;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.builders.WitnessBuilder;

public class WitnessSet {
  private final List<Witness> _witnesses;

  public WitnessSet(Witness... witnesses) {
    this(Arrays.asList(witnesses));
  }

  public WitnessSet(List<Witness> witnesses) {
    this._witnesses = witnesses;
  }

  public static WitnessSet createWitnessSet(String[] witnessStrings) {
    WitnessBuilder builder = new WitnessBuilder();
    int i = 1;
    List<Witness> witnesses = Lists.newArrayList();
    for (String witnessString : witnessStrings) {
      Witness witness = builder.build("witness" + i++, witnessString);
      witnesses.add(witness);
    }
    WitnessSet set = new WitnessSet(witnesses);
    return set;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Witness witness : _witnesses) {
      builder.append(witness.id + ": " + witness.toString() + "\n");
    }
    return builder.toString();
  }

  public List<Witness> getWitnesses() {
    return _witnesses;
  }
}
