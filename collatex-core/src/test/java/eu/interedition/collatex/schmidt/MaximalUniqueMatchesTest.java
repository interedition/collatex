package eu.interedition.collatex.schmidt;

import com.google.common.collect.Iterables;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MaximalUniqueMatchesTest extends AbstractTest {

  @Test
  public void find() {
    final SimpleWitness[] w = createWitnesses(
            "this morning the cat observed little birds in the trees",
            "the cat was observing birds in the little trees this morning it observed birds for two hours"
    );

    final MaximalUniqueMatches mums = MaximalUniqueMatches.find(new EqualityTokenComparator(), collate(w[0]), w[1]);

    for (Map.Entry<List<VariantGraph.Vertex>, List<Integer>> match : mums.entrySet()) {
      System.out.printf("%s == %s\n", Iterables.toString(match.getKey()), Iterables.toString(match.getValue()));
    }
  }
}
