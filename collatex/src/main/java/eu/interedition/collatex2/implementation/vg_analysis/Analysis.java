/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
  public List<ITransposition> getTranspositions() {
    List<ISequence> sequencesSortedForBase = sortSequencesForBase();
    if (sequencesSortedForBase.size()!=sequences.size()) {
      throw new RuntimeException("Something went wrong in the linking process!");
    }
    final List<ITransposition> transpositions = Lists.newArrayList();
    for (int i = 0; i < sequences.size(); i++) {
      final ISequence sequenceWitness = sequences.get(i);
      final ISequence sequenceBase = sequencesSortedForBase.get(i);
      if (!sequenceWitness.equals(sequenceBase)) {
        // TODO: I have got no idea why have to mirror the sequences here!
        transpositions.add(new Transposition(sequenceBase, sequenceWitness));
      }
    }
    return transpositions;
  }

  private List<ISequence> sortSequencesForBase() {
    // prepare map
    Map<INormalizedToken, ISequence> tokenToSequenceMap = Maps.newLinkedHashMap();
    for (ISequence sequence : sequences) {
      INormalizedToken firstToken = sequence.getBasePhrase().getFirstToken();
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
