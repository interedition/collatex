package eu.interedition.collatex.input;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class WitnessSet {
  private final List<Witness> witnesses;

  public WitnessSet(Witness... _witnesses) {
    this(Arrays.asList(_witnesses));
  }

  public WitnessSet(List<Witness> _witnesses) {
    this.witnesses = _witnesses;
  }

  // TODO: move this to alignmentTable2! 
  // TODO: make this a factory method!
  public AlignmentTable2 createAlignmentTable() {
    AlignmentTable2 table = new AlignmentTable2();
    for (Witness witness : witnesses) {
      table.addWitness(witness);
    }
    return table;
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
    for (Witness witness : witnesses) {
      builder.append(witness.id + ": " + witness.toString() + "\n");
    }
    return builder.toString();
  }
}
