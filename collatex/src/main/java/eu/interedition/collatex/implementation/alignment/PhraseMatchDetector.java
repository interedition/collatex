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
package eu.interedition.collatex.implementation.alignment;

import com.google.common.collect.Lists;
import eu.interedition.collatex.implementation.Tuple;
import eu.interedition.collatex.implementation.alignment.VariantGraphWitnessAdapter.VariantGraphVertexTokenAdapter;
import eu.interedition.collatex.interfaces.IWitness;
import eu.interedition.collatex.interfaces.Token;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Ronald
 */
public class PhraseMatchDetector {

  public List<Tuple<List<Token>>> detect(Map<Token, Token> linkedTokens, IWitness base, IWitness witness) {
    //rank the variant graph
    VariantGraphWitnessAdapter adapter = (VariantGraphWitnessAdapter) base;
    adapter.getGraph().rank();
    
    final List<Tuple<List<Token>>> phraseMatches = Lists.newArrayList();

    // chain token matches
    final List<Token> basePhrase = Lists.newArrayList();
    final List<Token> witnessPhrase = Lists.newArrayList();
    int previousRank = 1;

    //TODO: previous rank should be influenced by ommissions!
    for (Token token : witness.getTokens()) {
      if (!linkedTokens.containsKey(token)) {
        continue;
      }
      Token baseToken = linkedTokens.get(token);
      VariantGraphVertexTokenAdapter a = (VariantGraphVertexTokenAdapter) baseToken;
      int rank = a.getVertex().getRank();
      //see todo above: difference will not always be 0 or 1!
      if ((rank - previousRank) != (0 | 1)) {
        if (!basePhrase.isEmpty()) {
          // start a new sequence
          phraseMatches.add(new Tuple<List<Token>>(Lists.newArrayList(basePhrase), Lists.newArrayList(witnessPhrase)));
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
      phraseMatches.add(new Tuple<List<Token>>(Lists.newArrayList(basePhrase), Lists.newArrayList(witnessPhrase)));
    }
    return phraseMatches;
  }
}
