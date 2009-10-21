package eu.interedition.collatex.matching;

import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class Poging2 {
  private final Witness[] witnesses;
  private static Map<String, Witness> witnessHash = Maps.newHashMap();
  private Map<String, Map<String, List<Integer>>> sequences;

  Function<Word, Integer> extractPosition = new Function<Word, Integer>() {
    @Override
    public Integer apply(Word word0) {
      return Integer.valueOf(word0.position);
    }
  };

  public Poging2(Witness... _witnesses) {
    this.witnesses = _witnesses;

    for (Witness witness : witnesses) {
      witnessHash.put(witness.id, witness);
    }
  }

  void go() {
    sequences = getOneWordSequences();
  }

  Map<String, Map<String, List<Integer>>> getOneWordSequences() {
    Map<String, Map<String, List<Integer>>> oneWordSequences = Maps.newHashMap();
    for (Witness witness : witnesses) {
      //      Util.p(witness);
      for (Word word : witness.getWords()) {
        final String wordToMatch = word.normalized;
        if (!oneWordSequences.containsKey(wordToMatch)) {
          oneWordSequences.put(wordToMatch, matchingWordPositionsPerWitness(wordToMatch));
        }
      }
    }
    return oneWordSequences;
  }

  private Predicate<Word> matchingPredicate(final String wordToMatch) {
    Predicate<Word> matching = new Predicate<Word>() {
      @Override
      public boolean apply(Word word1) {
        return word1.normalized.equals(wordToMatch);
      }
    };
    return matching;
  }

  public Map<String, List<Integer>> matchingWordPositionsPerWitness(String wordToMatch) {
    Predicate<Word> matching = matchingPredicate(wordToMatch);
    Map<String, List<Integer>> map = Maps.newHashMap();
    for (Witness witness : witnesses) {
      String witnessId = witness.id;
      Iterable<Word> matchingWords = Iterables.filter(witness.getWords(), matching);
      Iterable<Integer> positions = Iterables.transform(matchingWords, extractPosition);
      map.put(witnessId, Lists.newArrayList(positions));
    }
    return map;
  }
}
