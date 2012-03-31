/*
 * Copyright 2010-2012 The Interedition Development Group.
 *
 * TODO: change license to GPL
 */
package eu.interedition.collatex.dekker;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphEdge;
import eu.interedition.collatex.graph.VariantGraphVertex;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Ronald
 */
public class PhraseMatchDetector {

  public List<List<Match>> detect(Map<Token, VariantGraphVertex> linkedTokens, VariantGraph base, Iterable<Token> tokens) {
    List<List<Match>> phraseMatches = Lists.newArrayList();
    List<VariantGraphVertex> basePhrase = Lists.newArrayList();
    List<Token> witnessPhrase = Lists.newArrayList();
    VariantGraphVertex previous = base.getStart();
 
    for (Token token : tokens) {
      if (!linkedTokens.containsKey(token)) {
        continue;
      }
      VariantGraphVertex baseVertex = linkedTokens.get(token);
      // requirements:
      // - there should a directed edge between previous and base vertex
      // - there may not be a longer path between previous and base vertex
      boolean directedEdge = directedEdgeBetween(previous, baseVertex);
      boolean isNear = directedEdge && (Iterables.size(previous.outgoing()) == 1 || Iterables.size(baseVertex.incoming()) == 1);
      if (!isNear) {
        if (!basePhrase.isEmpty()) {
          phraseMatches.add(Match.createPhraseMatch(basePhrase, witnessPhrase));
          basePhrase.clear();
          witnessPhrase.clear();
        }
      }
      basePhrase.add(baseVertex);
      witnessPhrase.add(token);
      previous = baseVertex;
    }
    if (!basePhrase.isEmpty()) {
      phraseMatches.add(Match.createPhraseMatch(basePhrase, witnessPhrase));
    }
    return phraseMatches;
  }

  private boolean directedEdgeBetween(VariantGraphVertex previous, VariantGraphVertex baseVertex) {
    Set<VariantGraphEdge> outgoing = Sets.newHashSet(previous.outgoing());
    Set<VariantGraphEdge> incoming = Sets.newHashSet(baseVertex.incoming());
    Set<VariantGraphEdge> intersection = Sets.intersection(outgoing, incoming);
    return !intersection.isEmpty();
  }
}
