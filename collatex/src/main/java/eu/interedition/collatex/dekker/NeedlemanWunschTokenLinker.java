package eu.interedition.collatex.dekker;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NeedlemanWunschTokenLinker implements TokenLinker {

  private float[][] matrix;
  private List<VariantGraphVertex> unlinkedVertices;
  private List<Token> unlinkedTokens;

  public float[][] getMatrix() {
    return matrix;
  }

  public List<VariantGraphVertex> getUnlinkedVertices() {
    return unlinkedVertices;
  }

  public List<Token> getUnlinkedTokens() {
    return unlinkedTokens;
  }

  @Override
  public Map<Token, VariantGraphVertex> link(VariantGraph graph, SortedSet<Token> witness, Comparator<Token> comparator) {
    final DefaultNeedlemanWunschScorer scorer = new DefaultNeedlemanWunschScorer(comparator);
    final List<VariantGraphVertex> vertexList = Lists.newArrayList(graph.vertices());
    final List<Token> tokenList = Lists.newArrayList(witness);
    final Map<Token, VariantGraphVertex> links = Maps.newHashMapWithExpectedSize(tokenList.size());

    matrix = new float[vertexList.size() + 1][tokenList.size() + 1];

    int ac = 0;
    int bc = 0;
    for (VariantGraphVertex vertex : vertexList) {
      matrix[ac++][0] = scorer.gap() * ac;
    }
    for (Token token : tokenList) {
      matrix[0][bc++] = scorer.gap() * bc;
    }

    ac = 1;
    for (VariantGraphVertex vertex : graph.vertices()) {
      bc = 1;
      for (Token token : witness) {
        final float k = matrix[ac - 1][bc - 1] + scorer.score(vertex, token);
        final float l = matrix[ac - 1][bc] + scorer.gap();
        final float m = matrix[ac][bc - 1] + scorer.gap();
        matrix[ac++][bc++] = Math.max(Math.max(k, l), m);
      }
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
        links.put(tokenList.get(bc - 1), vertexList.get(ac - 1));
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

    return links;
  }
}