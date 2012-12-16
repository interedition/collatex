package eu.interedition.collatex.needlemanwunsch;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.neo4j.VariantGraph;
import eu.interedition.collatex.neo4j.VariantGraphVertex;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NeedlemanWunschAlgorithm extends CollationAlgorithm.Base {

  private final Comparator<Token> comparator;
  private float[][] matrix;
  private List<Set<VariantGraphVertex>> unlinkedVertices;
  private List<Token> unlinkedTokens;

  public NeedlemanWunschAlgorithm(Comparator<Token> comparator) {
    this.comparator = comparator;
  }

  public float[][] getMatrix() {
    return matrix;
  }

  public List<Set<VariantGraphVertex>> getUnlinkedVertices() {
    return unlinkedVertices;
  }

  public List<Token> getUnlinkedTokens() {
    return unlinkedTokens;
  }

  @Override
  public void collate(VariantGraph against, Iterable<Token> witness) {
    final DefaultNeedlemanWunschScorer scorer = new DefaultNeedlemanWunschScorer(comparator);

    final List<Set<VariantGraphVertex>> vertexList = Lists.newArrayList(against.rank().ranks());
    final List<Token> tokenList = Lists.newArrayList(witness);

    final Map<Token, VariantGraphVertex> alignments = Maps.newHashMap();
    matrix = new float[vertexList.size() + 1][tokenList.size() + 1];
    unlinkedVertices = Lists.newArrayListWithCapacity(vertexList.size());
    unlinkedTokens = Lists.newArrayListWithCapacity(tokenList.size());

    int ac = 0;
    int bc = 0;
    while (ac < vertexList.size()) {
      matrix[ac++][0] = scorer.gap() * ac;
    }
    while (bc < tokenList.size()) {
      matrix[0][bc++] = scorer.gap() * bc;
    }

    ac = 1;
    for (Set<VariantGraphVertex> vertices : vertexList) {
      bc = 1;
      for (Token token : tokenList) {
        final float k = matrix[ac - 1][bc - 1] + scorer.score(vertices, token);
        final float l = matrix[ac - 1][bc] + scorer.gap();
        final float m = matrix[ac][bc - 1] + scorer.gap();
        matrix[ac][bc++] = Math.max(Math.max(k, l), m);
      }
      ac++;
    }

    ac = vertexList.size();
    bc = tokenList.size();
    while (ac > 0 && bc > 0) {
      final float score = matrix[ac][bc];
      final float scoreDiag = matrix[ac - 1][bc - 1];
      final float scoreUp = matrix[ac][bc - 1];
      final float scoreLeft = matrix[ac - 1][bc];

      if (score == scoreDiag + scorer.score(vertexList.get(ac - 1), tokenList.get(bc - 1))) {
        // match
        final Token matchedToken = tokenList.get(bc - 1);
        for (VariantGraphVertex vertex : vertexList.get(ac - 1)) {
          if (comparator.compare(Iterables.getFirst(vertex.tokens(), null), matchedToken) == 0) {
            if (LOG.isTraceEnabled()) {
              LOG.trace("Matched {} and {}", matchedToken, vertex);
            }
            alignments.put(matchedToken, vertex);
            break;
          }
        }
        ac--;
        bc--;
      } else if (score == scoreLeft + scorer.gap()) {
        // b omitted
        unlinkedVertices.add(vertexList.get(ac - 1));
        ac--;
      } else if (score == scoreUp + scorer.gap()) {
        // a omitted
        unlinkedTokens.add(tokenList.get(bc - 1));
        bc--;
      }
    }

    // fill-up
    while (ac > 0) {
      unlinkedVertices.add(vertexList.get(ac - 1));
      ac--;
    }
    while (bc > 0) {
      unlinkedTokens.add(tokenList.get(bc - 1));
      bc--;
    }

    merge(against, tokenList, alignments);
  }
}