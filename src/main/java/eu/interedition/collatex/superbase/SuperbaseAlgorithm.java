package eu.interedition.collatex.superbase;

import java.util.Arrays;
import java.util.List;

import eu.interedition.collatex.input.Witness;

// Note: this class has become more like a container for all the witnesses!
public class SuperbaseAlgorithm {
  private final List<Witness> witnesses;

  // NOTE: instead of comparing each of the witnesses with
  // each other.. the solution chosen here is based on a
  // superbase. So that every witness is compared against
  // the super base which is constructed after each compare

  public SuperbaseAlgorithm(Witness... _witnesses) {
    this(Arrays.asList(_witnesses));
  }

  public SuperbaseAlgorithm(List<Witness> _witnesses) {
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
