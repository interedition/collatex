package eu.interedition.collatex2.implementation.alignment;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IGap;

public class SequenceDetection {
  public static IAlignment improveAlignment(final IAlignment alignment) {
    final List<IGap> unfilteredGaps = alignment.getGaps();
    final List<IGap> filteredGaps = Lists.newArrayList();
    for (final IGap gap : unfilteredGaps) {
      if (!gap.isEmpty()) {
        filteredGaps.add(gap);
      }
    }
    return new Alignment(alignment.getMatches(), filteredGaps);
  }
}
