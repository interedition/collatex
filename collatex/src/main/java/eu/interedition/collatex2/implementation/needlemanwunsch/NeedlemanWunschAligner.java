package eu.interedition.collatex2.implementation.needlemanwunsch;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import eu.interedition.collatex2.implementation.Tuple;
import eu.interedition.collatex2.implementation.matching.EditDistance;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NeedlemanWunschAligner<T> {

  private final Scorer<T> scorer;

  private float[][] matrix;
  private List<Tuple<T>> alignments;

  public NeedlemanWunschAligner(Scorer<T> scorer) {
    this.scorer = scorer;
  }

  public List<Tuple<T>> getAlignments() {
    return alignments;
  }

  public float[][] getMatrix() {
    return matrix;
  }

  public static void main(String[] arg) {
    final NeedlemanWunschAligner<String> nw = new NeedlemanWunschAligner<String>(new Scorer<String>() {
      @Override
      public float score(String a, String b) {
        return (-1) * EditDistance.compute(a, b);
      }

      @Override
      public float gap() {
        return -10;
      }
    });

    final List<Tuple<String>> alignments = nw.align(//
            Arrays.asList("Hello World".split("\\s")),//
            Arrays.asList("Hello mighty World".split("\\s")));

    for (Tuple<String> al : alignments) {
      System.out.printf(Strings.isNullOrEmpty(al.left) ? "---" : al.left).printf("<===>").printf(Strings.isNullOrEmpty(al.right) ? "---" : al.right).println();
    }
  }

  public List<Tuple<T>> align(List<T> a, List<T> b) {
    createMatrix(a, b);
    deriveAlignment(a, b);
    return getAlignments();
  }

  public void deriveAlignment(List<T> a, List<T> b) {
    int ac = a.size();
    int bc = b.size();

    alignments = Lists.newArrayListWithExpectedSize(Math.max(ac, bc));
    while (ac > 0 && bc > 0) {
      final float score = matrix[ac][bc];
      final float scoreDiag = matrix[ac - 1][bc - 1];
      final float scoreUp = matrix[ac][bc - 1];
      final float scoreLeft = matrix[ac - 1][bc];

      if (score == scoreDiag + scorer.score(a.get(ac - 1), b.get(bc - 1))) {
        // match
        alignments.add(0, new Tuple<T>(a.get(ac - 1), b.get(bc - 1)));
        ac--;
        bc--;
      } else if (score == scoreLeft + scorer.gap()) {
        // b omitted
        alignments.add(0, new Tuple<T>(a.get(ac - 1), null));
        ac--;
      } else if (score == scoreUp + scorer.gap()) {
        // a omitted
        alignments.add(0, new Tuple<T>(null, b.get(bc - 1)));
        bc--;
      }
    }

    // fill-up
    while (ac > 0) {
      alignments.add(0, new Tuple<T>(a.get(ac - 1), null));
      ac--;
    }
    while (bc > 0) {
      alignments.add(0, new Tuple<T>(null, b.get(bc - 1)));
      bc--;
    }
  }

  public void createMatrix(List<T> a, List<T> b) {
    matrix = new float[a.size() + 1][b.size() + 1];

    for (int ac = 0; ac < a.size(); ac++) {
      matrix[ac][0] = scorer.gap() * ac;
    }
    for (int bc = 0; bc < b.size(); bc++) {
      matrix[0][bc] = scorer.gap() * bc;
    }

    for (int ac = 1; ac < a.size() + 1; ac++) {
      for (int bc = 1; bc < b.size() + 1; bc++) {
        final float k = matrix[ac - 1][bc - 1] + scorer.score(a.get(ac - 1), b.get(bc - 1));
        final float l = matrix[ac - 1][bc] + scorer.gap();
        final float m = matrix[ac][bc - 1] + scorer.gap();
        matrix[ac][bc] = Math.max(Math.max(k, l), m);
      }
    }
  }
}