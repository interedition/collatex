/*
 * Copyright 2011 The Interedition Development Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.interedition.collatex.alignment;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.interedition.collatex.IWitness;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Ronald
 */
public class PhraseMatchDetector {

  public List<List<Match>> detect(Map<Token, VariantGraphVertex> linkedTokens, VariantGraph base, IWitness witness) {
    //rank the variant graph
    base.rank();
    
    List<List<Match>> phraseMatches = Lists.newArrayList();

    // gather matched ranks into a set ordered by their natural order
    final Set<Integer> rankSet = Sets.newTreeSet();
    for (VariantGraphVertex vertex : linkedTokens.values()) {
      rankSet.add(vertex.getRank());
    }
 
    //Turn it into a List so that distance between matched ranks can be called
    //Note that omitted vertices are not in the list, so they don't cause an extra phrasematch
    List<Integer> ranks = Lists.newArrayList(rankSet);

    // chain token matches
    List<VariantGraphVertex> basePhrase = Lists.newArrayList();
    List<Token> witnessPhrase = Lists.newArrayList();
    int previousRank = 1;

    for (Token token : witness.getTokens()) {
      //Note: this if skips added tokens so they don't cause an extra phrasematch
      if (!linkedTokens.containsKey(token)) {
        continue;
      }
      VariantGraphVertex baseToken = linkedTokens.get(token);
      int rank = baseToken.getRank();
      int indexOfRank = ranks.indexOf(rank);
      int indexOfPreviousRank = ranks.indexOf(previousRank);
      int difference = indexOfRank - indexOfPreviousRank;
      if (difference != 0 && difference != 1) {
        if (!basePhrase.isEmpty()) {
          // start a new sequence
          phraseMatches.add(Match.createPhraseMatch(basePhrase, witnessPhrase));
        }
        // clear buffer
        basePhrase.clear();
        witnessPhrase.clear();
      }
      basePhrase.add(baseToken);
      witnessPhrase.add(token);
      previousRank = rank;
    }
    if (!basePhrase.isEmpty()) {
      phraseMatches.add(Match.createPhraseMatch(basePhrase, witnessPhrase));
    }
    return phraseMatches;
  }
}
