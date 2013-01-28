package eu.interedition.collatex;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.neo4j.Neo4jVariantGraph;
import eu.interedition.collatex.simple.SimpleVariantGraphSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface CollationAlgorithm {

  void collate(VariantGraph against, Iterable<Token> witness);

  void collate(VariantGraph against, Iterable<Token>... witnesses);

  void collate(VariantGraph against, List<? extends Iterable<Token>> witnesses);

  abstract class Base implements CollationAlgorithm {
    protected final Logger LOG = Logger.getLogger(getClass().getName());
    private Map<Token, VariantGraph.Vertex> witnessTokenVertices;

    @Override
    public void collate(VariantGraph against, Iterable<Token>... witnesses) {
      collate(against, Arrays.asList(witnesses));
    }

    @Override
    public void collate(VariantGraph against, List<? extends Iterable<Token>> witnesses) {
      for (Iterable<Token> witness : witnesses) {
        if (LOG.isLoggable(Level.FINE)) {
          LOG.log(Level.FINE, "heap space: {0}/{1}", new Object[] {
                  Runtime.getRuntime().totalMemory(),
                  Runtime.getRuntime().maxMemory()
          });
        }
        collate(against, witness);
      }
    }

    protected void merge(VariantGraph into, Iterable<Token> witnessTokens, Map<Token, VariantGraph.Vertex> alignments) {
      Preconditions.checkArgument(!Iterables.isEmpty(witnessTokens), "Empty witness");
      final Witness witness = Iterables.getFirst(witnessTokens, null).getWitness();

      if (LOG.isLoggable(Level.FINE)) {
        LOG.log(Level.FINE, "{0} + {1}: Merge comparand into graph", new Object[] { into, witness });
      }
      witnessTokenVertices = Maps.newHashMap();
      VariantGraph.Vertex last = into.getStart();
      final Set<Witness> witnessSet = Collections.singleton(witness);
      for (Token token : witnessTokens) {
        VariantGraph.Vertex matchingVertex = alignments.get(token);
        if (matchingVertex == null) {
          matchingVertex = into.add(token);
        } else {
          if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "Adding matched {0} to {1}", new Object[] { token, matchingVertex });
          }
          matchingVertex.add(Collections.singleton(token));
        }
        witnessTokenVertices.put(token, matchingVertex);

        into.connect(last, matchingVertex, witnessSet);
        last = matchingVertex;
      }
      into.connect(last, into.getEnd(), witnessSet);
    }

    protected void mergeTranspositions(VariantGraph into, List<List<Match>> transpositions) {
      for (List<Match> transposedPhrase : transpositions) {
        if (LOG.isLoggable(Level.FINE)) {
          LOG.log(Level.FINE, "transposition: {0}", transposedPhrase);
        }
        final Set<VariantGraph.Vertex> transposed = Sets.newHashSet();
        for (Match match : transposedPhrase) {
          transposed.add(witnessTokenVertices.get(match.token));
          transposed.add(match.vertex);
        }
        into.transpose(transposed);
      }
    }
  }
}
