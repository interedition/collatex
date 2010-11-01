package eu.interedition.collatex2.implementation.vg_analysis;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;


public class Analysis implements IAnalysis {
  private final List<ISequence> sequences;

  public Analysis(List<ISequence> sequences) {
    this.sequences = sequences;
  }
  
  @Override
  public List<ISequence> getSequences() {
    return sequences;
  }
  
  //TODO: Method is disabled, because it used getPosition method!
  //TODO: rewrite functionality to work with sorting on positions!
  @Override
  public List<ITransposition2> getTranspositions() {
//    final List<ISequence> matchesA = sequences;
//    final List<ISequence> matchesB = getSequencesSortedForWitness();
    final List<ITransposition2> transpositions = Lists.newArrayList();
//    for (int i = 0; i < matchesA.size(); i++) {
//      final ISequence matchA = matchesA.get(i);
//      final ISequence matchB = matchesB.get(i);
//      if (!matchA.equals(matchB)) {
//        // TODO: I have got no idea why have to mirror the matches here!
//        transpositions.add(new Transposition2(matchB, matchA));
//      }
//    }
    return transpositions;
  }

  final Comparator<ISequence> SORT_MATCHES_ON_POSITION_WITNESS = new Comparator<ISequence>() {
    @Override
    public int compare(final ISequence o1, final ISequence o2) {
      return o1.getPhraseB().getBeginPosition() - o2.getPhraseB().getBeginPosition();
    }
  };

  private List<ISequence> getSequencesSortedForWitness() {
    final List<ISequence> matchesForWitness = Lists.newArrayList(sequences);
    Collections.sort(matchesForWitness, SORT_MATCHES_ON_POSITION_WITNESS);
    return matchesForWitness;
  }


}
