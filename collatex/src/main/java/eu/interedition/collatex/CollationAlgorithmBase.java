package eu.interedition.collatex;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class CollationAlgorithmBase implements CollationAlgorithm {
  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  @Override
  public void collate(VariantGraph against, Iterable<Token>... witnesses) {
    collate(against, Arrays.asList(witnesses));
  }

  @Override
  public void collate(VariantGraph against, List<Iterable<Token>> witnesses) {
    for (Iterable<Token> witness : witnesses) {
      collate(against, witness);
    }
  }

  protected void merge(VariantGraph into, Iterable<Token> witnessTokens, Map<Token, VariantGraphVertex> alignments, Map<Token, VariantGraphVertex> transpositions) {
    Preconditions.checkArgument(!Iterables.isEmpty(witnessTokens), "Empty witness");
    final Witness witness = Iterables.getFirst(witnessTokens, null).getWitness();

    LOG.debug("{} + {}: Merge comparand into graph", into, witness);
    final Map<Token, VariantGraphVertex> witnessTokenVertices = Maps.newHashMap();
    VariantGraphVertex last = into.getStart();
    final Set<Witness> witnessSet = Collections.singleton(witness);
    for (Token token : witnessTokens) {
      VariantGraphVertex matchingVertex = alignments.get(token);
      if (matchingVertex == null) {
        if (LOG.isTraceEnabled()) {
          LOG.trace("Creating new vertex for unmatched {}", token);
        }
        matchingVertex = into.add(token);
      } else {
        if (LOG.isTraceEnabled()) {
          LOG.trace("Adding matched {} to {}", token, matchingVertex);
        }
        matchingVertex.add(Collections.singleton(token));
      }
      witnessTokenVertices.put(token, matchingVertex);

      into.connect(last, matchingVertex, witnessSet);
      if (LOG.isTraceEnabled()) {
        LOG.trace("Connected {} and {}", last, matchingVertex);
      }
      last = matchingVertex;
    }
    into.connect(last, into.getEnd(), witnessSet);
    if (LOG.isTraceEnabled()) {
      LOG.trace("Connected {} and {}", last, into.getEnd());
    }

    LOG.debug("{}: Registering transpositions", into);
    for (Token token : transpositions.keySet()) {
      into.transpose(transpositions.get(token), witnessTokenVertices.get(token));
    }
  }
}
