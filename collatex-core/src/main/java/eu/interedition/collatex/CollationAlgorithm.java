package eu.interedition.collatex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.simple.SimpleVariantGraphSerializer;

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
        //        toDotFile("inmerge", into);
      }
      into.connect(last, into.getEnd(), witnessSet);
    }

    protected void mergeTranspositions(VariantGraph into, List<List<Match>> transpositions) {
      for (List<Match> transposedPhrase : transpositions) {
        int transpositionId = RandomUtils.nextInt(327680000);//transposedPhrase.hashCode();
        final Map<Token, VariantGraphVertex> transposedTokens = Maps.newHashMap();
        LOG.info("transposition: {}, hash={}", transposedPhrase, transpositionId);
        for (Match match : transposedPhrase) {
          transposedTokens.put(match.token, match.vertex);
        }
        for (Token token : transposedTokens.keySet()) {
          into.transpose(transposedTokens.get(token), witnessTokenVertices.get(token), transpositionId);
        }
      }
    }

    void toDotFile(String filename, VariantGraph vg) {
      try {
        PrintWriter writer = new PrintWriter(new File("out/" + filename + ".dot"), "UTF-8");
        new SimpleVariantGraphSerializer(vg).toDot(vg, writer);
        writer.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }
  }

}
