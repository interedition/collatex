package eu.interedition.collatex2.interfaces;

public interface ITokenMatcher {
  List<ITokenMatch> getMatches(IWitness witness);
}
