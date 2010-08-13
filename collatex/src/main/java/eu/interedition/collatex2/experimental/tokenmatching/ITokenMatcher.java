package eu.interedition.collatex2.experimental.tokenmatching;

import java.util.List;

import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;

public interface ITokenMatcher {
  List<ITokenMatch> getMatches(IWitness witness);
}
