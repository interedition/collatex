package eu.interedition.collatex2.implementation.vg_analysis;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenContainer;


public class Analysis implements IAnalysis {
  private final List<ISequence> sequences;
  private final ITokenContainer base;

  public Analysis(List<ISequence> sequences, ITokenContainer base) {
    this.sequences = sequences;
    this.base = base;
  }
  
  @Override
  public List<ISequence> getSequences() {
    return sequences;
  }
  
  @Override
  public List<ITransposition2> getTranspositions() {
    List<ISequence> sequencesSortedForBase = sortSequencesForBase();
    final List<ITransposition2> transpositions = Lists.newArrayList();
    for (int i = 0; i < sequences.size(); i++) {
      final ISequence sequenceWitness = sequences.get(i);
      final ISequence sequenceBase = sequencesSortedForBase.get(i);
      if (!sequenceWitness.equals(sequenceBase)) {
        // TODO: I have got no idea why have to mirror the sequences here!
        transpositions.add(new Transposition2(sequenceBase, sequenceWitness));
      }
    }
    return transpositions;
  }

  private List<ISequence> sortSequencesForBase() {
    // prepare map
    Map<INormalizedToken, ISequence> tokenToSequenceMap = Maps.newLinkedHashMap();
    for (ISequence sequence : sequences) {
      //NOTE:  THIS IS WEIRD! should be sequence.getBasePhrase! 
      INormalizedToken firstToken = sequence.getWitnessPhrase().getFirstToken();
      tokenToSequenceMap.put(firstToken, sequence);
    }
    // sort sequences
    List<ISequence> orderedSequences = Lists.newArrayList();
    Iterator<INormalizedToken> tokenIterator = base.tokenIterator();
    while(tokenIterator.hasNext()) {
      INormalizedToken token = tokenIterator.next();
      if (tokenToSequenceMap.containsKey(token)) {
        ISequence sequence = tokenToSequenceMap.get(token);
        orderedSequences.add(sequence);
      }
    }
    return orderedSequences;
  }
}
