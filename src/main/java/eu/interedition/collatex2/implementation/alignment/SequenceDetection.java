package eu.interedition.collatex2.implementation.alignment;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.implementation.alignmenttable.Columns;
import eu.interedition.collatex2.implementation.input.Phrase;
import eu.interedition.collatex2.implementation.matching.Match;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IColumn;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IGap;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class SequenceDetection {
  public static IAlignment improveAlignment(final IAlignment alignment) {
    final List<IMatch> chainedMatches = chainMatches(alignment);
    final List<IGap> filteredGaps = filterEmptyGaps(alignment);
    return new Alignment(chainedMatches, filteredGaps);
  }

  // NOTE: we might want to extract the two tokens lists into a 
  // token container or something!
  private static List<IMatch> chainMatches(final IAlignment alignment) {
    // input/output
    final List<IMatch> unchainedMatches = alignment.getMatches();
    final List<IMatch> chainedMatches = Lists.newArrayList();
    // calculate previous matches map for A and B
    final List<IMatch> matchesSortedForA = unchainedMatches;
    final List<IMatch> matchesSortedForB = alignment.getMatchesSortedForB();
    // now build the actual map!
    final Map<IMatch, IGap> previousGapMapB = SequenceDetection.buildPreviousGapMap(matchesSortedForB, alignment.getGaps());
    final Map<IMatch, IMatch> previousMatchMapA = SequenceDetection.buildPreviousMatchMap(matchesSortedForA);
    final Map<IMatch, IMatch> previousMatchMapB = SequenceDetection.buildPreviousMatchMap(matchesSortedForB);
    // make buffer
    List<IColumn> columnsA = Lists.newArrayList();
    List<INormalizedToken> tokensB = Lists.newArrayList();
    // chain the matches
    for (int index = 0; index < unchainedMatches.size(); index++) {
      final IMatch match = unchainedMatches.get(index);
      // determine whether matches should be chained
      final IGap previousGapA = alignment.getGaps().get(index);
      final IGap previousGapB = previousGapMapB.get(match);
      final IMatch previousMatchA = previousMatchMapA.get(match);
      final IMatch previousMatchB = previousMatchMapB.get(match);
      if (!previousGapA.isEmpty() || !previousGapB.isEmpty() || previousMatchA != previousMatchB) {
        createChainedMatchAndAddToList(chainedMatches, columnsA, tokensB);
        columnsA = Lists.newArrayList();
        tokensB = Lists.newArrayList();
      }
      // fill buffer
      columnsA.add(match.getColumnsA().getFirstColumn());
      tokensB.add(match.getPhraseB().getFirstToken());
    }
    createChainedMatchAndAddToList(chainedMatches, columnsA, tokensB);
    return chainedMatches;
  }

  private static Map<IMatch, IGap> buildPreviousGapMap(final List<IMatch> matches, final List<IGap> gaps) {
    final Map<IMatch, IGap> previousGaps = Maps.newHashMap();
    for (int index = 0; index < matches.size(); index++) {
      final IGap previousGap = gaps.get(index);
      final IMatch match = matches.get(index);
      previousGaps.put(match, previousGap);
    }
    return previousGaps;
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

  private static void createChainedMatchAndAddToList(final List<IMatch> chainedMatches, final List<IColumn> columnsA, final List<INormalizedToken> tokensB) {
    if (!columnsA.isEmpty()) {
      final IColumns columns = new Columns(columnsA);
      final Phrase phrase = new Phrase(tokensB);
      chainedMatches.add(new Match(columns, phrase));
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
