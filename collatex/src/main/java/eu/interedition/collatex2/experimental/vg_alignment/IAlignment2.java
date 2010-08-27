package eu.interedition.collatex2.experimental.vg_alignment;

import java.util.List;

import eu.interedition.collatex2.interfaces.ITokenMatch;

public interface IAlignment2 {

  List<IMatch2> getMatches();

  List<ITransposition2> getTranspositions();

  List<ITokenMatch> getTokenMatches();

}
