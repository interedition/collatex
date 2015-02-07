package eu.interedition.collatex.dekker.matrix;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class IslandConflictResolverTest extends AbstractTest {
  
  // 3 islands of 2, 1 island of size 1
  // the 3 islands of size 2 overlap partly
  //TODO: add new IslandCompetitionType: party overlapping!
  @Test
  public void testPartlyOverlappingIslands() {
    // create two witnesses
    SimpleWitness[] w = createWitnesses("The cat and the dog", "the dog and the cat");
    // create graph from the first witness
    VariantGraph graph = collate(w[0]);
    // create table from the graph and the second witness
    MatchTable table = MatchTable.create(graph, w[1]);
    List<Island> possibleIslands = new ArrayList<>();
    for (Island island : table.getIslands()) {
      if (island.size()==2) {
        possibleIslands.add(island);
      }
    }
    IslandConflictResolver resolver = new IslandConflictResolver(table);
    Map<IslandCompetition, List<Island>> competition = resolver.analyzeConflictsBetweenPossibleIslands(possibleIslands);
    assertEquals(3, competition.get(IslandCompetition.CompetingIsland).size());
  }
}
