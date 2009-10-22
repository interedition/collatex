package eu.interedition.collatex.matching;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.sd_editions.collatex.Block.Util;

import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;

public class Poging2 {
  private final Segment[] witnesses;
  private static Map<String, Segment> witnessHash = Maps.newHashMap();
  private Map<String, Map<String, List<Integer>>> sequences;

  Function<Word, Integer> extractPosition = new Function<Word, Integer>() {
    @Override
    public Integer apply(Word word0) {
      return Integer.valueOf(word0.position);
    }
  };

  public Poging2(Segment... _witnesses) {
    this.witnesses = _witnesses;

    for (Segment witness : witnesses) {
      witnessHash.put(witness.id, witness);
    }
  }

  void go() {
    sequences = getOneWordSequences();
    Multimap<WitnessPosition, String> sequencesAtWitnessPosition = Multimaps.newArrayListMultimap();
    Set<Entry<String, Map<String, List<Integer>>>> entrySet = sequences.entrySet();
    for (Entry<String, Map<String, List<Integer>>> entry : entrySet) {
      String sequenceTitle = entry.getKey();
      Map<String, List<Integer>> positionsPerWitness = entry.getValue();
      Set<Entry<String, List<Integer>>> entrySet2 = positionsPerWitness.entrySet();
      for (Entry<String, List<Integer>> positionsPerWitnessEntry : entrySet2) {
        String witnessId = positionsPerWitnessEntry.getKey();
        List<Integer> positions = positionsPerWitnessEntry.getValue();
        for (Integer position : positions) {
          WitnessPosition witnessPosition = new WitnessPosition(witnessId, position);
          sequencesAtWitnessPosition.put(witnessPosition, sequenceTitle);
        }
      }
      Util.p(sequenceTitle + " occurs in " + positionsPerWitness.size() + " witnesses.");
      Util.p(sequencesAtWitnessPosition);
    }

    Iterator<WitnessPosition> iterator = sequencesAtWitnessPosition.keys().iterator();
    WitnessPosition dummy = iterator.next();
    WitnessPosition first = iterator.next();
    // see if wordsegement at position first is expandable
    WitnessPosition next = first.nextWitnessPosition();
    Collection<String> sequencesForFirst = sequencesAtWitnessPosition.get(first);
    Util.p(first);
    Util.p(sequencesForFirst);
    Collection<String> sequencesForNext = sequencesAtWitnessPosition.get(next);
    Util.p(next);
    Util.p(sequencesForNext);

    Collection<String> commonSequences = findCommonSequences(sequencesForFirst, sequencesForNext);
    Util.p(commonSequences);
  }

  Collection<String> findCommonSequences(Collection<String> sequences0, Collection<String> sequences1) {
    List<String> commonSequences = Lists.newArrayList();
    int size0 = sequences0.size();
    int size1 = sequences1.size();
    if (size0 < size1) {
      commonSequences = addToCommonSequences(sequences0, sequences1, commonSequences);
    } else {
      commonSequences = addToCommonSequences(sequences1, sequences0, commonSequences);
    }
    return commonSequences;
  }

  private List<String> addToCommonSequences(Collection<String> sequences0, Collection<String> sequences1, List<String> commonSequences) {
    for (String sequenceTitle : sequences0) {
      if (sequences1.contains(sequenceTitle)) commonSequences.add(sequenceTitle);
    }
    return commonSequences;
  }

  Map<String, Map<String, List<Integer>>> getOneWordSequences() {
    Map<String, Map<String, List<Integer>>> oneWordSequences = Maps.newHashMap();
    for (Segment witness : witnesses) {
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
    Predicate<Word> matchingPredicate = matchingPredicate(wordToMatch);
    Map<String, List<Integer>> map = Maps.newHashMap();
    for (Segment witness : witnesses) {
      String witnessId = witness.id;
      Iterable<Word> matchingWords = Iterables.filter(witness.getWords(), matchingPredicate);
      Iterable<Integer> matchingWordPositions = Iterables.transform(matchingWords, extractPosition);
      List<Integer> positions = Lists.newArrayList(matchingWordPositions);
      if (!positions.isEmpty()) map.put(witnessId, positions);
    }
    return map;
  }
}
