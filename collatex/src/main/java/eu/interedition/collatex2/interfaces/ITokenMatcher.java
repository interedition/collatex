package eu.interedition.collatex2.interfaces;

import java.util.List;


public interface ITokenMatcher {
  List<ITokenMatch> getMatches(IWitness witness);
}
