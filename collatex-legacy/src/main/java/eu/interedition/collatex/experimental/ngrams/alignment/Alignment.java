package eu.interedition.collatex.experimental.ngrams.alignment;

import java.util.List;

import eu.interedition.collatex.experimental.ngrams.NGram;
import eu.interedition.collatex2.interfaces.IWitness;

public class Alignment {

  private final List<NGram> matches;
  private final List<Gap> gaps;

  public Alignment(final List<NGram> matches, final List<Gap> gaps) {
    this.matches = matches;
    this.gaps = gaps;
  }

  public List<NGram> getMatches() {
    return matches;
  }

  public List<Gap> getGaps() {
    return gaps;
  }

  public static Alignment create(final IWitness a, final IWitness b) {
    final WitnessSet set = new WitnessSet(a, b);
    return set.align();
  }

  public void accept(final ModificationVisitor modificationVisitor) {
    for (final Gap gap : gaps) {
      final Modification modification = gap.getModification();
      modification.accept(modificationVisitor);
    }
  }
}
