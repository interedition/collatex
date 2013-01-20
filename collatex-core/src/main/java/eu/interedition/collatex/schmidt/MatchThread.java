package eu.interedition.collatex.schmidt;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
class MatchThread {

  private final MatchThread previous;
  private final SuffixTree<Token>.Cursor cursor;
  private final VariantGraph.Vertex vertex;

  MatchThread(SuffixTree<Token> suffixTree) {
    this(null, suffixTree.cursor(), null);
  }

  MatchThread(MatchThread previous, SuffixTree<Token>.Cursor cursor, VariantGraph.Vertex vertex) {
    this.previous = previous;
    this.cursor = cursor;
    this.vertex = vertex;
  }

  MatchThread advance(VariantGraph.Vertex vertex, Set<Range<Integer>> excludedTokens) {
    final Set<Token> tokens = vertex.tokens();
    if (!tokens.isEmpty()) {
      final SuffixTree<Token>.Cursor next = cursor.move(Iterables.get(tokens, 0));
      if (next != null) {
        SuffixTree<Token>.EquivalenceClass match = next.matchedClass();
        for (int mc = 0; mc < match.length; mc++) {
          boolean notExcluded = true;
          for (Range<Integer> excludedRange : excludedTokens) {
            if (excludedRange.contains(match.members[mc])) {
              notExcluded = false;
              break;
            }
          }
          if (notExcluded) {
            return new MatchThread(this, next, vertex);
          }
        }
      }
    }
    return null;
  }

  static Function<MatchThread,List<Match>> toMatches(final Set<Range<Integer>> excludedTokens) {
    return new Function<MatchThread, List<Match>>() {
      @Override
      public List<Match> apply(@Nullable MatchThread input) {
        final List<Match> matches = Lists.newLinkedList();

        MatchThread current = input;
        int lastToken = -1;
        while (current.vertex != null) {
          final SuffixTree<Token>.EquivalenceClass matchedClass = current.cursor.matchedClass();

          boolean foundMatch = false;
          for (int mc = 0; mc < matchedClass.length; mc++) {
            if (lastToken >= 0 && lastToken != (matchedClass.members[mc] + 1)) {
              continue;
            }
            boolean notExcluded = true;
            for (Range<Integer> excludedRange : excludedTokens) {
              if (excludedRange.contains(matchedClass.members[mc])) {
                notExcluded = false;
                break;
              }
            }
            if (notExcluded) {
              matches.add(0, new Match(current.vertex, lastToken = matchedClass.members[mc]));
              foundMatch = true;
              break;
            }
          }
          Preconditions.checkState(foundMatch, "Cannot reconstruct matches from thread");
          current = current.previous;
        }

        return matches;
      }
    };
  };

}
