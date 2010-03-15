package eu.interedition.collatex2.implementation.alignment;

import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.indexing.NGram;
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
  //WARNING: Only looking at the next match for witness A is not
  //good enough, since transpositions can change order of the
  //matches in witness B!
  private static List<IMatch> chainMatches(final IAlignment alignment) {
    final List<IMatch> unchainedMatches = alignment.getMatches();
    final List<IMatch> chainedMatches = Lists.newArrayList();
    List<INormalizedToken> tokensA = Lists.newArrayList();
    List<INormalizedToken> tokensB = Lists.newArrayList();
    for (int index = 0; index < unchainedMatches.size(); index++) {
      final IMatch match = unchainedMatches.get(index);
      tokensA.add(match.getNGramA().getFirstToken());
      tokensB.add(match.getNGramB().getFirstToken());
      final IGap nextGap = alignment.getGaps().get(index + 1);
      if (!nextGap.isEmpty()) {
        createChainedMatchAndAddToList(chainedMatches, tokensA, tokensB);
        tokensA = Lists.newArrayList();
        tokensB = Lists.newArrayList();
      }
    }
    createChainedMatchAndAddToList(chainedMatches, tokensA, tokensB);
    return chainedMatches;
  }

  private static void createChainedMatchAndAddToList(final List<IMatch> chainedMatches, final List<INormalizedToken> tokensA, final List<INormalizedToken> tokensB) {
    if (!tokensA.isEmpty()) {
      final NGram phraseA = new NGram(tokensA);
      final NGram phraseB = new NGram(tokensB);
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
