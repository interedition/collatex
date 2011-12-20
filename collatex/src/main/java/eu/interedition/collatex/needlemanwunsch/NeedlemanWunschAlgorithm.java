package eu.interedition.collatex.needlemanwunsch;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.collatex.CollationAlgorithmBase;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.TokenLinker;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NeedlemanWunschAlgorithm extends CollationAlgorithmBase {

  private final Comparator<Token> comparator;
  private float[][] matrix;
  private List<VariantGraphVertex> unlinkedVertices;
  private List<Token> unlinkedTokens;

  public NeedlemanWunschAlgorithm(Comparator<Token> comparator) {
    this.comparator = comparator;
  }


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
  protected void collate(VariantGraph against, SortedSet<Token> witness) {
    final DefaultNeedlemanWunschScorer scorer = new DefaultNeedlemanWunschScorer(comparator);

    final List<VariantGraphVertex> vertexList = Lists.newArrayList(against.vertices());
    vertexList.remove(0);
    vertexList.remove(vertexList.size() - 1);

    final List<Token> tokenList = Lists.newArrayList(witness);

    final SortedMap<Token, VariantGraphVertex> alignments = Maps.newTreeMap();
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
    for (VariantGraphVertex vertex : vertexList) {
      bc = 1;
      for (Token token : tokenList) {
        final float k = matrix[ac - 1][bc - 1] + scorer.score(vertex, token);
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
        alignments.put(tokenList.get(bc - 1), vertexList.get(ac - 1));
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
    
    merge(against, tokenList, alignments, Collections.<Token, VariantGraphVertex>emptyMap());
  }
}