package eu.interedition.collatex2.interfaces;

import java.util.List;

public interface IAlignment {

  List<IMatch> getMatches();

  List<IMatch> getMatchesSortedForWitness();

  List<IGap> getGaps();

  List<ITransposition> getTranspositions();

  List<IAddition> getAdditions();

  List<IReplacement> getReplacements();

  List<IOmission> getOmissions();

}
