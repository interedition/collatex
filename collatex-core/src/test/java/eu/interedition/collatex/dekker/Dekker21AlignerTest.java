package eu.interedition.collatex.dekker;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.matrix.*;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import java.util.*;

import static eu.interedition.collatex.dekker.VariantGraphMatcher.graph;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class Dekker21AlignerTest extends AbstractTest {

    // helper method
    // note: x = x of start coordinate
    // note: y = y of start coordinate
    //TODO: replace Island by a real Vector class
    private void assertIslandAsVectorEquals(int x, int y, int length, Collection<Island> islands) {
        Coordinate startCoordinate = new Coordinate(x, y);
        Coordinate endCoordinate = new Coordinate(x+length-1, y+length-1);
        Island expected = new Island(startCoordinate, endCoordinate);
        assertTrue(islands.contains(expected));
    }

    @Test
    public void testExample1() {
        final SimpleWitness[] w = createWitnesses("This morning the cat observed little birds in the trees.", "The cat was observing birds in the little trees this morning, it observed birds for two hours.");
        Dekker21Aligner aligner = new Dekker21Aligner(w);
        List<Block> blocks = aligner.tokenIndex.getNonOverlappingBlocks();
        Set<String> blocksAsString = new HashSet<>();
        blocks.stream().map(interval -> interval.getNormalizedForm()).forEach(blocksAsString::add);
        Set<String> expected = new HashSet<>(Arrays.asList("birds in the", "the cat", "this morning", ".", "little", "observed", "trees"));
        assertEquals(expected, blocksAsString);
        List<Block.Instance> instances = aligner.tokenIndex.getBlockInstancesForWitness(w[0]);
        assertEquals("[this morning, the cat, observed, little, birds in the, trees, .]", instances.toString());
        instances = aligner.tokenIndex.getBlockInstancesForWitness(w[1]);
        assertEquals("[the cat, birds in the, little, trees, this morning, observed, .]", instances.toString());

        VariantGraph g = new VariantGraph();
        aligner.collate(g, w[0]);

        aligner.collate(g, w[1]);

        Set<Island> islands = aligner.table.getIslands();
        assertIslandAsVectorEquals(0, 2, 2, islands); // the cat
        assertIslandAsVectorEquals(4, 6, 3, islands); // birds in the
        assertIslandAsVectorEquals(7, 5, 1, islands); // little
        assertIslandAsVectorEquals(8, 9, 1, islands); // trees
        assertIslandAsVectorEquals(9, 0, 2, islands); // this morning
        assertIslandAsVectorEquals(13, 4, 1, islands); // observed
        assertIslandAsVectorEquals(18, 10, 1, islands); // .
        assertEquals(7, islands.size());

        List<Island> selectedIslands = aligner.preferredIslands;
        assertIslandAsVectorEquals(0, 2, 2, selectedIslands); // the cat
        assertIslandAsVectorEquals(4, 6, 3, selectedIslands); // birds in the
        assertIslandAsVectorEquals(7, 5, 1, selectedIslands); // little
        assertIslandAsVectorEquals(8, 9, 1, selectedIslands); // trees
        assertIslandAsVectorEquals(9, 0, 2, selectedIslands); // this morning
        assertIslandAsVectorEquals(13, 4, 1, selectedIslands); // observed
        assertIslandAsVectorEquals(18, 10, 1, selectedIslands); // .
        assertEquals(7, selectedIslands.size());

        //Todo: assert transpositions
//        assertPhraseMatches("this morning", "observed", "little");
//        System.out.println(aligner.transpositions);

        assertThat(g, graph(w[0]).non_aligned("this", "morning").aligned("the", "cat").non_aligned("observed").non_aligned("little").aligned("birds", "in", "the").aligned("trees", "."));
        assertThat(g, graph(w[1]).aligned("the", "cat").non_aligned("was", "observing").aligned("birds", "in", "the").non_aligned("little").aligned("trees").non_aligned("this", "morning").non_aligned(",", "it").non_aligned("observed", "birds", "for", "two", "hours").aligned("."));
    }
}
