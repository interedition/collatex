package eu.interedition.collatex2.implementation;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.alignment.Alignment;
import eu.interedition.collatex2.implementation.alignment.GapDetection;
import eu.interedition.collatex2.implementation.alignment.SequenceDetection;
import eu.interedition.collatex2.implementation.alignmenttable.AlignmentTableCreator3;
import eu.interedition.collatex2.implementation.indexing.WitnessIndex;
import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.implementation.matching.Match;
import eu.interedition.collatex2.implementation.matching.RealMatcher;
import eu.interedition.collatex2.implementation.matching.worddistance.NormalizedLevenshtein;
import eu.interedition.collatex2.implementation.matching.worddistance.WordDistance;
import eu.interedition.collatex2.implementation.tokenization.NormalizedWitnessBuilder;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

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
    final Phrase a = Phrase.create(baseWord);
    final Phrase b = Phrase.create(witnessWord);
    return new Match(a, b);
  }

  public static IMatch createMatch(final INormalizedToken baseWord, final INormalizedToken witnessWord, final float editDistance) {
    throw new RuntimeException("Near matches are not yet supported!");
  }

  public static IMatch createMatch(final IPhrase basePhrase, final IPhrase witnessPhrase, final float editDistance) {
    throw new RuntimeException("Near matches are not yet supported!");
  }

  public static IWitnessIndex createWitnessIndex(final IWitness witness) {
    final WitnessIndex witnessIndex = new WitnessIndex(witness);
    return witnessIndex;
  }

  public IAlignmentTable createNewAlignmentTable(final List<IWitness> set) {
    IAlignmentTable table;
    table = AlignmentTableCreator3.createAlignmentTable(set);
    return table;
  }

}
