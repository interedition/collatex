package eu.interedition.collatex.input;

import java.util.Arrays;
import java.util.List;

import eu.interedition.collatex.superbase.AlignmentTable2;

public class WitnessSet {
  private final List<Witness> witnesses;

  public WitnessSet(Witness... _witnesses) {
    this(Arrays.asList(_witnesses));
  }

  public WitnessSet(List<Witness> _witnesses) {
    this.witnesses = _witnesses;
  }

  public AlignmentTable2 createAlignmentTable() {
    AlignmentTable2 table = new AlignmentTable2();
    for (Witness witness : witnesses) {
      table.addWitness(witness);
    }
    return table;
  }
}
