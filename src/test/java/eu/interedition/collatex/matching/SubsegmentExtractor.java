package eu.interedition.collatex.matching;

import java.util.Collection;
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
import com.google.common.collect.Sets;
import com.sd_editions.collatex.Block.Util;

import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;

public class SubsegmentExtractor {
  private final Segment[] witnesses;
  private static Map<String, Segment> witnessHash = Maps.newHashMap();
  //  private Map<String, Map<String, List<Integer>>> subsegments;
  private Subsegments subsegments;

  Function<Word, Integer> extractPosition = new Function<Word, Integer>() {
    @Override
    public Integer apply(Word word0) {
      return Integer.valueOf(word0.position);
    }
  };

  public SubsegmentExtractor(Segment... _witnesses) {
    this.witnesses = _witnesses;

    for (Segment witness : witnesses) {
      Util.p(witness.id, witness);
      witnessHash.put(witness.id, witness);
    }
    Util.newline();
  }

  void go() {
    subsegments = getOneWordSubsegments();

    Multimap<SegmentPosition, String> sequencesAtSegmentPosition = getSequencesAtSegmentPosition();
    Util.p("sequencesAtSegmentPosition", sequencesAtSegmentPosition);

    Subsegment subsegment = subsegments.getFirstOpenSubsegment();
    while (subsegment != null) {
      Util.newline();
      Util.p("subsegment", subsegment);

      Set<String> witnessIdsForSubsegment = subsegment.getWitnessIds();
      //      Util.p(witnessIdsForSubsegment);

      Set<String> nextSubsegmentTitleSet = Sets.newHashSet();
      for (SegmentPosition segmentPosition : subsegment.getSegmentPositions()) {
        SegmentPosition next = segmentPosition.nextSegmentPosition();
        nextSubsegmentTitleSet.add(subsegments.getSubsegmentTitleAtSegmentPosition(next));
      }
      Util.p("nextSubsegmentTitleSet", nextSubsegmentTitleSet);
      if (nextSubsegmentTitleSet.size() == 1 && !nextSubsegmentTitleSet.contains(null)) {
        // subsegment and nextSubsegment can be joined
        Util.remark("join!");
        subsegments.join(subsegment.getTitle(), nextSubsegmentTitleSet.iterator().next());
      } else {}

      subsegments.close(subsegment.getTitle());
      subsegment = subsegments.getFirstOpenSubsegment();

      //      Set<Entry<String, List<Integer>>> entrySet = subsegment.entrySet();
      //      for (Entry<String, List<Integer>> entry : entrySet) {
      //        String witnessId = entry.getKey();
      //        List<Integer> positions = entry.getValue();
      //      }
    }

    // sequences: "zijn", "hond", "liep", "aan", "hand", "op", "pad", "met", "hij"

    // neem: zijn
    // zijn is in witness a,b,c
    // nextwords_for_zijn: hond, hand,pad
    //  hond in a,b,c => "zijn hond" is een sequence
    //  hand in a,b,c => "zijn hand" is een sequence
    //  pad in b,c => "zijn pad" is een sequence
    // verwijder zijn, hond, hand en pad als sequences
    // voeg toe: "zijn hond", "zijn hand", "zijn pad"

    // sequences: "zijn hond", "liep", "aan", "zijn hand", "op", "zijn pad", "met", "hij"

    // groei "zijn hond": a:liep,b:aan,c:aan => sequence is final
    // groei "zijn hand": a:-,b:-,c:liep => final
    // groei "zijn pad": b:liep,c:- => final

    // sequences: "zijn hond"!, "liep", "aan", "zijn hand"!, "op", "zijn pad"!, "met", "hij"

    // neem "liep"
    // in witness a,b,c
    // nextwords": aan,zijn,hij
    // nextwords.size=(a,b,c).size => liep is final

    // sequences: "zijn hond"!, "liep"!, "aan", "zijn hand"!, "op", "zijn pad"!, "met", "hij"

    // neem "aan"
    // in witness a,b,c
    // nextwords: zijn (onderdeel van "zijn hand"
    // nextwords.size=(a,b,c).size => "aan zijn hand" is nieuwe sequence
    // verwijder "aan" en "zijn hand"
    // voeg "aan zijn hand" toe. "zijn hand" was final, dus is "aan zijn hand" dat ook

    // sequences: "zijn hond"!, "liep"!, "aan zijn hand"!, "op", "zijn pad"!, "met", "hij"

    // "op"
    // in witness b,c
    // nextwords: zijn (onderdeel van "zijn pad")
    // nextwords.size=(b,c).size => "op zijn pad" is nieuwe sequence
    // verwijder "op" en "zijn pad"
    // voeg "op zijn pad" toe. "zijn pad" was final, dus is "op zijn pad" dat ook

    // sequences: "zijn hond"!, "liep"!, "aan zijn hand"!, "op zijn pad"!, "met", "hij"

    // "met"
    // in witness c
    // nextword: zijn (onderdeel van "zijn hond", dat andere witnesses heeft dan met => met is final) 

    // sequences: "zijn hond"!, "liep"!, "aan zijn hand"!, "op zijn pad"!, "met"!, "hij"

    // "hij"
    // in witness c
    // nextword: op (onderdeel van "op zijn pad", dat andere witnesses heeft dan met => hij is final)

    // sequences: "zijn hond"!, "liep"!, "aan zijn hand"!, "op zijn pad"!, "met"!, "hij"!

    // geen woorden meer: klaar!

    // sequences: "zijn hond (a1-2,b)"!, "liep"!, "aan zijn hand"!, "op zijn pad"!, "met"!, "hij"!

    //    Iterator<SegmentPosition> iterator = sequencesAtSegmentPosition.keys().iterator();
    //    SegmentPosition dummy = iterator.next();
    //    SegmentPosition first = iterator.next();
    //    // see if wordsegement at position first is expandable
    //    SegmentPosition next = first.nextSegmentPosition();
    //    Collection<String> sequencesForFirst = sequencesAtSegmentPosition.get(first);
    //    Util.p(first);
    //    Util.p(sequencesForFirst);
    //    Collection<String> sequencesForNext = sequencesAtSegmentPosition.get(next);
    //    Util.p(next);
    //    Util.p(sequencesForNext);
    //
    //    Collection<String> commonSequences = findCommonSequences(sequencesForFirst, sequencesForNext);
    //    Util.p(commonSequences);
  }

  private Multimap<SegmentPosition, String> getSequencesAtSegmentPosition() {
    Multimap<SegmentPosition, String> sequencesAtSegmentPosition = Multimaps.newArrayListMultimap();
    for (Subsegment subsegment : subsegments.all()) {
      String sequenceTitle = subsegment.getTitle();
      for (Entry<String, List<Integer>> positionsPerWitnessEntry : subsegment.entrySet()) {
        String witnessId = positionsPerWitnessEntry.getKey();
        List<Integer> positions = positionsPerWitnessEntry.getValue();
        for (Integer position : positions) {
          SegmentPosition segmentPosition = new SegmentPosition(witnessId, position);
          sequencesAtSegmentPosition.put(segmentPosition, sequenceTitle);
        }
      }
      //      Util.p(sequenceTitle + " occurs in " + subsegment.size() + " witnesses.");
      //      Util.p(sequencesAtSegmentPosition);
    }
    return sequencesAtSegmentPosition;
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

  Subsegments getOneWordSubsegments() {
    Subsegments oneWordSequences = new Subsegments();
    for (Segment witness : witnesses) {
      //      Util.p(witness);
      for (Word word : witness.getWords()) {
        final String wordToMatch = word.normalized;
        if (!oneWordSequences.containsTitle(wordToMatch)) {
          oneWordSequences.add(wordToMatch, matchingWordPositionsPerWitness(wordToMatch));
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

  public Subsegment matchingWordPositionsPerWitness(String wordToMatch) {
    Predicate<Word> matchingPredicate = matchingPredicate(wordToMatch);
    //    Map<String, List<Integer>> map = Maps.newHashMap();
    Subsegment subsegment = new Subsegment(wordToMatch);
    for (Segment witness : witnesses) {
      String witnessId = witness.id;
      Iterable<Word> matchingWords = Iterables.filter(witness.getWords(), matchingPredicate);
      Iterable<Integer> matchingWordPositions = Iterables.transform(matchingWords, extractPosition);
      List<Integer> positions = Lists.newArrayList(matchingWordPositions);
      if (!positions.isEmpty()) subsegment.add(witnessId, positions);
    }
    return subsegment;
  }

  public Subsegments getSubsegments() {
    return subsegments;
  }
}
