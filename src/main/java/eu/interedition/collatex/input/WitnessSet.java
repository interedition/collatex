package eu.interedition.collatex.input;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.builders.WitnessBuilder;
import eu.interedition.collatex.input.visitors.JSONObjectVisitor;

public class WitnessSet {
  private final List<Segment> _witnesses;

  public WitnessSet(Segment... witnesses) {
    this(Arrays.asList(witnesses));
  }

  public WitnessSet(List<Segment> witnesses) {
    this._witnesses = witnesses;
  }

  public static WitnessSet createWitnessSet(String[] witnessStrings) {
    WitnessBuilder builder = new WitnessBuilder();
    int i = 1;
    List<Segment> witnesses = Lists.newArrayList();
    for (String witnessString : witnessStrings) {
      Segment witness = builder.build("witness" + i++, witnessString);
      witnesses.add(witness);
    }
    WitnessSet set = new WitnessSet(witnesses);
    return set;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Segment witness : _witnesses) {
      builder.append(witness.id + ": " + witness.toString() + "\n");
    }
    return builder.toString();
  }

  public List<Segment> getWitnesses() {
    return _witnesses;
  }

  public void accept(JSONObjectVisitor visitor) {
    visitor.visitWitnessSet(this);
    for (Segment witness : _witnesses) {
      witness.accept(visitor);
    }
  }
}
