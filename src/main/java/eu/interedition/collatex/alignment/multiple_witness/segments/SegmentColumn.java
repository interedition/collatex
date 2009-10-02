package eu.interedition.collatex.alignment.multiple_witness.segments;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.MatchSequence;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class SegmentColumn {
  protected final Map<String, Segment> _segmentsProWitness;
  // TODO: add other segments!
  private final Segment _segment;

  public SegmentColumn(Segment segment) {
    this._segment = segment;
    this._segmentsProWitness = Maps.newLinkedHashMap();
    addMatch(segment); // TODO: make this add variant!
  }

  @Override
  public String toString() {
    return _segment.toString();
  }

  public void addToSuperbase(SegmentSuperbase superbase) {
    superbase.addSegment(_segment, this);
  }

  // TODO: what if multiple... take the first one?
  public Segment getSegment() {
    return _segment;
  }

  public void addMatch(Segment segment) {
    _segmentsProWitness.put(segment.getWitnessId(), segment);
    // TODO: add match to columnstate
  }

  // TODO: make a constructor in Segment for a MatchSequence?
  public void addMatchSequenceToColumn(MatchSequence seq) {
    // We have to convert the match sequence witness to a segment
    List<Match> matches = seq.getMatches();
    List<Word> witnessWords = Lists.newArrayList();
    for (Match match : matches) {
      witnessWords.add(match.getWitnessWord());
    }
    Segment newSegment = new Segment(witnessWords);
    addMatch(newSegment);
  }

  public boolean containsWitness(Witness witness) {
    return _segmentsProWitness.containsKey(witness.id);
  }

  public Segment getSegment(Witness witness) {
    if (!containsWitness(witness)) {
      throw new NoSuchElementException();
    }
    return _segmentsProWitness.get(witness.id);
  }
}
