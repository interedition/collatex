package eu.interedition.collatex2.implementation.alignment;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.implementation.matching.Match;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class SequenceDetection {
  public static IAlignment improveAlignment(final IAlignment alignment) {
    final List<IMatch> chainedMatches = chainMatches(alignment);
    final List<IGap> filteredGaps = filterEmptyGaps(alignment);
    return new Alignment(chainedMatches, filteredGaps);
  }

  //NOTE: we might want to extract the two tokens lists into a 
  // token container or something!
  //WARNING: Only looking at the previous match for witness A is not
  //good enough, since transpositions can change order of the
  //matches in witness B!
  private static List<IMatch> chainMatches(final IAlignment alignment) {
    // input/output
    final List<IMatch> unchainedMatches = alignment.getMatches();
    final List<IMatch> chainedMatches = Lists.newArrayList();
    // calculate previous matches map for A and B
    // TODO: add maps for previous gaps for A and B
    final List<IMatch> matchesSortedForA = unchainedMatches;
    final List<IMatch> matchesSortedForB = alignment.getMatchesSortedForB();
    // now build the actual map!
    final Map<IMatch, IMatch> previousMatchMapA = SequenceDetection.buildPreviousMatchMap(matchesSortedForA);
    final Map<IMatch, IMatch> previousMatchMapB = SequenceDetection.buildPreviousMatchMap(matchesSortedForB);
    // make buffer
    List<INormalizedToken> tokensA = Lists.newArrayList();
    List<INormalizedToken> tokensB = Lists.newArrayList();
    // chain the matches
    for (int index = 0; index < unchainedMatches.size(); index++) {
      final IMatch match = unchainedMatches.get(index);
      // determine whether matches should be chained
      final IGap previousGap = alignment.getGaps().get(index);
      final IMatch previousMatchA = previousMatchMapA.get(match);
      final IMatch previousMatchB = previousMatchMapB.get(match);
      if (!previousGap.isEmpty() || previousMatchA != previousMatchB) {
        createChainedMatchAndAddToList(chainedMatches, tokensA, tokensB);
        tokensA = Lists.newArrayList();
        tokensB = Lists.newArrayList();
      }
      // fill buffer
      tokensA.add(match.getPhraseA().getFirstToken());
      tokensB.add(match.getPhraseB().getFirstToken());
    }
    createChainedMatchAndAddToList(chainedMatches, tokensA, tokensB);
    return chainedMatches;
  }

  private static Map<IMatch, IMatch> buildPreviousMatchMap(final List<IMatch> matches) {
    final Map<IMatch, IMatch> previousMatches = Maps.newHashMap();
    IMatch previousMatch = null;
    for (final IMatch match : matches) {
      previousMatches.put(match, previousMatch);
      previousMatch = match;
    }
    return previousMatches;
  }

  private static void createChainedMatchAndAddToList(final List<IMatch> chainedMatches, final List<INormalizedToken> tokensA, final List<INormalizedToken> tokensB) {
    if (!tokensA.isEmpty()) {
      final Phrase phraseA = new Phrase(tokensA);
      final Phrase phraseB = new Phrase(tokensB);
      chainedMatches.add(new Match(phraseA, phraseB));
    }
  }

  private static List<IGap> filterEmptyGaps(final IAlignment alignment) {
    final List<IGap> unfilteredGaps = alignment.getGaps();
    final List<IGap> filteredGaps = Lists.newArrayList();
    for (final IGap gap : unfilteredGaps) {
      if (!gap.isEmpty()) {
        filteredGaps.add(gap);
      }
    }
    return filteredGaps;
  }
}
