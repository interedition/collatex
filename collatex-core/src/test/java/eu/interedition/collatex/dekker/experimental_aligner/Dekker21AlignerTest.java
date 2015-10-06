package eu.interedition.collatex.dekker.experimental_aligner;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.matrix.Coordinate;
import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class Dekker21AlignerTest extends AbstractTest {

    // helper method
    // note: x = start coordinate of witness token
    // note: y = start coordinate of base token
    //TODO: replace Island by a real Vector class
    private void assertIslandAsVectorEquals(int x, int y, int length, Collection<Island> islands) {
        Coordinate startCoordinate = new Coordinate(x, y);
        Coordinate endCoordinate = new Coordinate(x+length-1, y+length-1);
        Island expected = new Island(startCoordinate, endCoordinate);
        Assert.assertTrue("Islands are: "+islands, islands.contains(expected));
    }

    @Test
    public void testExample1() {
        final SimpleWitness[] w = createWitnesses("This morning the cat observed little birds in the trees.", "The cat was observing birds in the little trees this morning, it observed birds for two hours.");
        Dekker21Aligner aligner = new Dekker21Aligner();
        VariantGraph graph = new VariantGraph();
        List<SimpleWitness> witnesses = new ArrayList<>();
        witnesses.addAll(Arrays.asList(w));
        aligner.collate(graph, witnesses);
        List<Block> blocks = aligner.tokenIndex.getNonOverlappingBlocks();
        Set<String> blocksAsString = new HashSet<>();
        blocks.stream().map(interval -> interval.getNormalizedForm()).forEach(blocksAsString::add);
        Set<String> expected = new HashSet<>(Arrays.asList("birds in the", "the cat", "this morning", ".", "little", "observed", "trees"));
        Assert.assertEquals(expected, blocksAsString);
        List<Block.Instance> instances = aligner.tokenIndex.getBlockInstancesForWitness(w[0]);
        Assert.assertEquals("[this morning, the cat, observed, little, birds in the, trees, .]", instances.toString());
        instances = aligner.tokenIndex.getBlockInstancesForWitness(w[1]);
        Assert.assertEquals("[the cat, birds in the, little, trees, this morning, observed, .]", instances.toString());

        List<Island> islands = aligner.getIslands();
        assertIslandAsVectorEquals(0, 2, 2, islands); // the cat
        assertIslandAsVectorEquals(4, 6, 3, islands); // birds in the
        assertIslandAsVectorEquals(7, 5, 1, islands); // little
        assertIslandAsVectorEquals(8, 9, 1, islands); // trees
        assertIslandAsVectorEquals(9, 0, 2, islands); // this morning
        assertIslandAsVectorEquals(13, 4, 1, islands); // observed
        assertIslandAsVectorEquals(18, 10, 1, islands); // .
        // When optimised it can be done with 7 islands instead 16
        Assert.assertEquals(16, islands.size());

        List<Island> selectedIslands = aligner.preferredIslands;
        assertIslandAsVectorEquals(0, 2, 2, selectedIslands); // the cat
        assertIslandAsVectorEquals(4, 6, 3, selectedIslands); // birds in the
        assertIslandAsVectorEquals(7, 5, 1, selectedIslands); // little
        assertIslandAsVectorEquals(8, 9, 1, selectedIslands); // trees
        assertIslandAsVectorEquals(9, 0, 2, selectedIslands); // this morning
        assertIslandAsVectorEquals(13, 4, 1, selectedIslands); // observed
        assertIslandAsVectorEquals(18, 10, 1, selectedIslands); // .
        Assert.assertEquals(7, selectedIslands.size());

        //Todo: assert transpositions
        //        assertPhraseMatches("this morning", "observed", "little");
        //        System.out.println(aligner.transpositions);

        Assert.assertThat(graph, VariantGraphMatcher.graph(w[0]).non_aligned("this", "morning").aligned("the", "cat").non_aligned("observed").non_aligned("little").aligned("birds", "in", "the").aligned("trees", "."));
        Assert.assertThat(graph, VariantGraphMatcher.graph(w[1]).aligned("the", "cat").non_aligned("was", "observing").aligned("birds", "in", "the").non_aligned("little").aligned("trees").non_aligned("this", "morning").non_aligned(",", "it").non_aligned("observed", "birds", "for", "two", "hours").aligned("."));
    }

}
