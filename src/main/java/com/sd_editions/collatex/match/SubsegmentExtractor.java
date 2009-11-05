package com.sd_editions.collatex.match;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Iterables.transform;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sd_editions.collatex.Block.Util;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.UnfixedAlignment;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.WitnessSegmentPhrases;
import eu.interedition.collatex.input.Word;

public class SubsegmentExtractor {
  private final Segment[] segments;
  private static Map<String, Segment> segmentHash = Maps.newHashMap();
  private Subsegments subsegments;

  Function<Word, Integer> extractPosition = new Function<Word, Integer>() {
    @Override
    public Integer apply(final Word word0) {
      return Integer.valueOf(word0.position);
    }
  };

  public SubsegmentExtractor(final Segment... _segments) {
    this.segments = _segments;
    for (final Segment segment : segments) {
      Util.p(segment.getWitnessId(), segment);
      segmentHash.put(segment.getWitnessId(), segment);
    }
    Util.newline();
  }

  private static final Predicate<Subsegment> NOT_SINGULAR = new Predicate<Subsegment>() {
    @Override
    public boolean apply(final Subsegment subsegment) {
      return !subsegment.isSingular();
    }
  };

  public void go() {
    subsegments = expandSubsegmentsUntilAllAreSingular(getOneWordSubsegments());

    Subsegment subsegment = subsegments.getFirstOpenSubsegment();
    while (subsegment != null) {
      Util.newline();
      Util.p("subsegment", subsegment);

      //      final Set<String> witnessIds = subsegment.getWitnessIds();

      final Map<String, List<SegmentPosition>> nextWordMap = Maps.newHashMap();
      for (final SegmentPosition segmentPosition : subsegment.getSegmentPositions()) {
        final SegmentPosition nextSegmentPosition = segmentPosition.nextSegmentPosition();
        final String nextSegmentTitle = subsegments.getSubsegmentTitleAtSegmentPosition(nextSegmentPosition);
        List<SegmentPosition> list = nextWordMap.get(nextSegmentTitle);
        if (list == null) list = Lists.newArrayList();
        list.add(nextSegmentPosition);
        nextWordMap.put(nextSegmentTitle, list);
      }
      Util.p("nextwordmap", nextWordMap);

      //      final Set<Entry<String, List<SegmentPosition>>> entrySet = nextWordMap.entrySet();
      //      for (final Entry<String, List<SegmentPosition>> entry : entrySet) {
      //        entry.getKey();
      //        entry.getValue();
      //      }

      final Set<String> nextSubsegmentTitleSet = Sets.newHashSet();
      for (final SegmentPosition segmentPosition : subsegment.getSegmentPositions()) {
        final SegmentPosition next = segmentPosition.nextSegmentPosition();
        nextSubsegmentTitleSet.add(subsegments.getSubsegmentTitleAtSegmentPosition(next));
      }
      Util.p("nextSubsegmentTitleSet", nextSubsegmentTitleSet);

      if (subsegment.size() > 1 && nextWordMap.size() == 1 && !nextSubsegmentTitleSet.contains(null)) {
        // subsegment and nextSubsegment can be joined
        Util.remark("join!");
        final List<SegmentPosition> startPositions = null;
        subsegment = subsegments.join(subsegment.getTitle(), nextSubsegmentTitleSet.iterator().next(), startPositions);
        Util.p(subsegment);
      } else {
        subsegments.close(subsegment.getTitle());
        subsegment = subsegments.getFirstOpenSubsegment();
      }
    }

    // sequences: "zijn", "hond", "liep", "aan", "hand", "op", "pad", "met", "hij"

    // neem: zijn
    // zijn is in witness a,b,c
    // nextwords_for_zijn: hond, hand, pad
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

  @SuppressWarnings("boxing")
  private Subsegments expandSubsegmentsUntilAllAreSingular(final Subsegments _subsegments) {
    final Map<SegmentPosition, String> sequencesAtSegmentPosition = getSequencesAtSegmentPosition(_subsegments);
    Util.p("sequencesAtSegmentPosition", sequencesAtSegmentPosition);

    final Iterable<Subsegment> pluralSubsegments = Lists.newArrayList(filter(_subsegments.all(), NOT_SINGULAR));
    final Map<String, Subsegment> nextWords = Maps.newHashMap();
    for (final Subsegment pluralSubsegment : pluralSubsegments) {
      final String subsegmentTitle = pluralSubsegment.getTitle();

      for (final String witnessId : pluralSubsegment.getWitnessIds()) {
        final List<Integer> positions = pluralSubsegment.get(witnessId);
        for (final Integer position : positions) {
          final String nextWord = sequencesAtSegmentPosition.get(new SegmentPosition(witnessId, position + 1));
          Subsegment subsegment = nextWords.get(nextWord);
          if (subsegment == null) subsegment = new Subsegment(subsegmentTitle);
          List<Integer> originalPositions = subsegment.get(witnessId);
          if (originalPositions == null) originalPositions = Lists.newArrayList();
          originalPositions.add(position);
          subsegment.add(witnessId, originalPositions);
          nextWords.put(nextWord, subsegment);
          final Subsegment originalSubsegmentForNextWord = _subsegments.get(nextWord);
          originalSubsegmentForNextWord.deleteSegmentPosition(new SegmentPosition(witnessId, position));
        }
      }
      Util.p("nextWords", nextWords);
      final Set<Entry<String, Subsegment>> entrySet = nextWords.entrySet();
      for (final Entry<String, Subsegment> entry : entrySet) {
        final String nextWord = entry.getKey();
        final Subsegment subsegment = entry.getValue();
        subsegment.extend(nextWord);
        _subsegments.add(subsegment.getTitle(), subsegment);
      }
      pluralSubsegment.markForRemoval();
      _subsegments.removeMarkedSubsegments();
    }
    Util.p("_subsegments", _subsegments);
    return _subsegments;
  }

  private Map<SegmentPosition, String> getSequencesAtSegmentPosition(final Subsegments _subsegments) {
    final Map<SegmentPosition, String> sequencesAtSegmentPosition = Maps.newHashMap();
    for (final Subsegment subsegment : _subsegments.all()) {
      final String sequenceTitle = subsegment.getTitle();
      for (final Entry<String, List<Integer>> positionsPerWitnessEntry : subsegment.entrySet()) {
        final String witnessId = positionsPerWitnessEntry.getKey();
        final List<Integer> positions = positionsPerWitnessEntry.getValue();
        for (final Integer position : positions) {
          sequencesAtSegmentPosition.put(new SegmentPosition(witnessId, position), sequenceTitle);
        }
      }
      //      Util.p(sequenceTitle + " occurs in " + subsegment.size() + " witnesses.");
      //      Util.p(sequencesAtSegmentPosition);
    }
    return sequencesAtSegmentPosition;
  }

  Collection<String> findCommonSequences(final Collection<String> sequences0, final Collection<String> sequences1) {
    List<String> commonSequences = Lists.newArrayList();
    final int size0 = sequences0.size();
    final int size1 = sequences1.size();
    if (size0 < size1) {
      commonSequences = addToCommonSequences(sequences0, sequences1, commonSequences);
    } else {
      commonSequences = addToCommonSequences(sequences1, sequences0, commonSequences);
    }
    return commonSequences;
  }

  private List<String> addToCommonSequences(final Collection<String> sequences0, final Collection<String> sequences1, final List<String> commonSequences) {
    for (final String sequenceTitle : sequences0) {
      if (sequences1.contains(sequenceTitle)) commonSequences.add(sequenceTitle);
    }
    return commonSequences;
  }

  public Subsegments getOneWordSubsegments() {
    final Subsegments oneWordSequences = new Subsegments();
    for (final Segment segment : segments) {
      for (final Word word : segment.getWords()) {
        final String wordToMatch = word.normalized;
        Util.p(wordToMatch);
        if (!oneWordSequences.containsTitle(wordToMatch)) {
          oneWordSequences.add(wordToMatch, matchingWordPositionsPerWitness(wordToMatch));
          Util.p(oneWordSequences);
        }
      }
    }
    return oneWordSequences;
  }

  private Predicate<Word> matchingPredicate(final String wordToMatch) {
    final Predicate<Word> matching = new Predicate<Word>() {
      @Override
      public boolean apply(final Word word1) {
        return word1.normalized.equals(wordToMatch);
      }
    };
    return matching;
  }

  public Subsegment matchingWordPositionsPerWitness(final String wordToMatch) {
    final Predicate<Word> matchingPredicate = matchingPredicate(wordToMatch);
    //    Map<String, List<Integer>> map = Maps.newHashMap();
    final Subsegment subsegment = new Subsegment(wordToMatch);
    for (final Segment segment : segments) {
      final String witnessId = segment.getWitnessId();
      final Iterable<Integer> matchingWordPositions = transform(filter(segment.getWords(), matchingPredicate), extractPosition);
      final List<Integer> positions = Lists.newArrayList(matchingWordPositions);
      if (!positions.isEmpty()) subsegment.add(witnessId, positions);
    }
    return subsegment;
  }

  public Subsegments getSubsegments() {
    return subsegments;
  }

  //  public Map<String, List<Phrase>> getPhrasesPerSegment() {
  //    final Map<String, List<Phrase>> phrasesPerSegment = Maps.newHashMap();
  //    for (final Segment segment : segments) {
  //      final List<Phrase> phraseList = subsegments.getPhrases(segment);
  //      phrasesPerSegment.put(segment.getWitnessId(), phraseList);
  //    }
  //    return phrasesPerSegment;
  //  }

  public WitnessSegmentPhrases getWitnessSegmentPhrases(final String witnessId) {
    subsegments.removeMarkedSubsegments();
    final Predicate<Segment> relevant = new Predicate<Segment>() {
      @Override
      public boolean apply(final Segment s) {
        return s.getWitnessId().equals(witnessId);
      }
    };
    final Segment witnessSegment = find(Lists.newArrayList(segments), relevant);
    final List<Phrase> phraseList = subsegments.getPhrases(witnessSegment);
    return new WitnessSegmentPhrases(witnessId, phraseList);
  }

  public UnfixedAlignment<Phrase> getUnfixedAlignment() {
    final Set<Match<Phrase>> fixed = Sets.newHashSet();
    final Set<Match<Phrase>> unfixed = Sets.newHashSet();
    return new UnfixedAlignment<Phrase>(fixed, unfixed);
  }
}
