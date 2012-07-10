package eu.interedition.collatex;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface CollationAlgorithm {

  void collate(VariantGraph against, Iterable<Token> witness);

  void collate(VariantGraph against, Iterable<Token>... witnesses);

  void collate(VariantGraph against, List<Iterable<Token>> witnesses);

  abstract class Base implements CollationAlgorithm {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());
    private Map<Token, VariantGraphVertex> witnessTokenVertices;

    @Override
    public void collate(VariantGraph against, Iterable<Token>... witnesses) {
      collate(against, Arrays.asList(witnesses));
    }

    @Override
    public void collate(VariantGraph against, List<Iterable<Token>> witnesses) {
      for (Iterable<Token> witness : witnesses) {
        LOG.debug("heap space: {}/{}", Runtime.getRuntime().totalMemory(), Runtime.getRuntime().maxMemory());
        collate(against, witness);
      }
    }

    protected void merge(VariantGraph into, Iterable<Token> witnessTokens, Map<Token, VariantGraphVertex> alignments) {
      Preconditions.checkArgument(!Iterables.isEmpty(witnessTokens), "Empty witness");
      final Witness witness = Iterables.getFirst(witnessTokens, null).getWitness();

      LOG.debug("{} + {}: Merge comparand into graph", into, witness);
      witnessTokenVertices = Maps.newHashMap();
      VariantGraphVertex last = into.getStart();
      final Set<Witness> witnessSet = Collections.singleton(witness);
      for (Token token : witnessTokens) {
        VariantGraphVertex matchingVertex = alignments.get(token);
        if (matchingVertex == null) {
          matchingVertex = into.add(token);
        } else {
          if (LOG.isTraceEnabled()) {
            LOG.trace("Adding matched {} to {}", token, matchingVertex);
          }
          matchingVertex.add(Collections.singleton(token));
        }
        witnessTokenVertices.put(token, matchingVertex);

        into.connect(last, matchingVertex, witnessSet);
        last = matchingVertex;
      }
      into.connect(last, into.getEnd(), witnessSet);
    }

    protected List<List<Match>> filterOutlierTranspositions(VariantGraph into, List<List<Match>> transpositions) {
      into.rank();
      LOG.debug("{}: Registering transpositions", into);
      List<List<Match>> filteredTranspositions = Lists.newArrayList(transpositions);
      for (List<Match> transposedPhrase : transpositions) {
        Match firstMatch = transposedPhrase.get(0);
        VariantGraphVertex from = firstMatch.vertex;
        Token token = firstMatch.token;
        VariantGraphVertex to = witnessTokenVertices.get(token);
        LOG.info("matchPhrase={}", transposedPhrase);
        int fromRank = from.getRank();
        //        LOG.info("from={}, rank={}", from, fromRank);
        int toRank = to.getRank();
        //        LOG.info("to={}, rank={}", to, toRank);
        int diff = Math.abs(toRank - fromRank);
        int size = transposedPhrase.size();

        int relDiff = diff / size;
        boolean acceptTransposition = relDiff < 5;
        LOG.info("accept={}, relDiff={}, size={}, diff={}, from={}, to={}\n", new Object[] { acceptTransposition, relDiff, size, diff, from, to });
        if (acceptTransposition) {
          for (Match match : transposedPhrase) {
            into.transpose(match.vertex, witnessTokenVertices.get(match.token));
          }
        } else {
          filteredTranspositions.remove(transposedPhrase);
        }
      }
      return filteredTranspositions;
    }

    protected void mergeTranspositions(VariantGraph into, List<List<Match>> transpositions) {
      final Map<Token, VariantGraphVertex> transposedTokens = Maps.newHashMap();
      for (List<Match> transposedPhrase : transpositions) {
        for (Match match : transposedPhrase) {
          transposedTokens.put(match.token, match.vertex);
        }
      }
      for (Token token : transposedTokens.keySet()) {
        into.transpose(transposedTokens.get(token), witnessTokenVertices.get(token));
      }
    }
  }
}
