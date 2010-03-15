package eu.interedition.collatex2.implementation;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.alignment.Alignment;
import eu.interedition.collatex2.implementation.alignment.GapDetection;
import eu.interedition.collatex2.implementation.alignment.SequenceDetection;
import eu.interedition.collatex2.implementation.indexing.NGram;
import eu.interedition.collatex2.implementation.matching.Match;
import eu.interedition.collatex2.implementation.matching.RealMatcher;
import eu.interedition.collatex2.implementation.matching.worddistance.NormalizedLevenshtein;
import eu.interedition.collatex2.implementation.matching.worddistance.WordDistance;
import eu.interedition.collatex2.implementation.tokenization.NormalizedWitnessBuilder;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class Factory {

  public IWitness createWitness(final String sigil, final String words) {
    return NormalizedWitnessBuilder.create(sigil, words);
  }

  public IAlignment createAlignment(final IWitness a, final IWitness b) {
    final WordDistance distanceMeasure = new NormalizedLevenshtein();
    final Set<IMatch> matches = RealMatcher.findMatches(a, b, distanceMeasure);
    final List<IMatch> matchesAsList = Lists.newArrayList(matches);
    final List<IGap> gaps = GapDetection.detectGap(matchesAsList, a, b);
    final IAlignment alignment = SequenceDetection.improveAlignment(new Alignment(matchesAsList, gaps));
    return alignment;
  }

  public static IMatch createMatch(final INormalizedToken baseWord, final INormalizedToken witnessWord) {
    final NGram a = NGram.create(baseWord);
    final NGram b = NGram.create(witnessWord);
    return new Match(a, b);
  }

  public static IMatch createMatch(final INormalizedToken baseWord, final INormalizedToken witnessWord, final float editDistance) {
    throw new RuntimeException("Near matches are not yet supported!");
  }

}
