package eu.interedition.collatex.interfaces;

import java.util.List;

public interface IAlignment {
  // NOTE: not sure whether this should be public
  List<INGram> getMatchesOrderedByWitnessA();

  // NOTE: not sure whether this should be public
  List<INGram> getMatchesOrderedByWitnessB();

  List<IGap> getGaps();

  // TODO: add getMatches and IMatch class!
}
