package eu.interedition.collatex2.experimental.graph;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.indexing.WitnessIndex;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

//NOTE: this could become a generic TokenMatcher with a little
//more extraction
public class AlignmentGraphWitnessMatcher {
  //TODO: do inversion of control for creation of indexes!!
  //TODO: take care of repeating tokens (same as IndexMatcher)!
  private final IAlignmentGraph graph;

  //TODO: extract IMatcher interface?
  public AlignmentGraphWitnessMatcher(IAlignmentGraph graph) {
    this.graph = graph;
  }
  
  public List<ITokenMatch> getMatches(IWitness witness) {
    final List<ITokenMatch> matches = Lists.newArrayList();
    List<String> repeatingTokens = Lists.newArrayList();
    IWitnessIndex witnessIndex = new WitnessIndex(witness, repeatingTokens);
    IAlignmentGraphIndex graphIndex = AlignmentGraphIndex.create(graph, repeatingTokens);
    final Collection<IPhrase> phrases = witnessIndex.getPhrases();
    for (final IPhrase phrase : phrases) {
      if (graphIndex.containsNormalizedPhrase(phrase.getNormalized())) {
        Collection<INormalizedToken> nodes = graphIndex.getTokens(phrase.getNormalized());
        Iterator<INormalizedToken> iterator = phrase.getTokens().iterator();
        for (INormalizedToken node : nodes) {
          INormalizedToken witnessToken = iterator.next();
          INormalizedToken matchingToken = node;
          matches.add(new TokenMatch(witnessToken, matchingToken));
        }
      }
    }
    return matches;
  }
  

}
