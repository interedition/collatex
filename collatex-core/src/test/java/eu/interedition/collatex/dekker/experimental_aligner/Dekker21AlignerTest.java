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
        Coordinate endCoordinate = new Coordinate(x + length - 1, y + length - 1);
        Island expected = new Island(startCoordinate, endCoordinate);
        Assert.assertTrue("Islands are: " + islands, islands.contains(expected));
    }

    @Test
    public void testExample1() {
        final SimpleWitness[] w = createWitnesses("This morning the cat observed little birds in the trees.", "The cat was observing birds in the little trees this morning, it observed birds for two hours.");
        Dekker21Aligner aligner = new Dekker21Aligner();
        VariantGraph graph = new VariantGraph();
        List<SimpleWitness> witnesses = new ArrayList<>();
        witnesses.addAll(Arrays.asList(w));
        aligner.collate(graph, witnesses);
//        List<Block> blocks = aligner.tokenIndex.getNonOverlappingBlocks();
//        Set<String> blocksAsString = new HashSet<>();
//        blocks.stream().map(interval -> interval.getNormalizedForm()).forEach(blocksAsString::add);
//        Set<String> expected = new HashSet<>(Arrays.asList("birds in the", "the cat", "this morning", ".", "little", "observed", "trees"));
//        Assert.assertEquals(expected, blocksAsString);
//        List<Block.Instance> instances = aligner.tokenIndex.getBlockInstancesForWitness(w[0]);
//        Assert.assertEquals("[this morning, the cat, observed, little, birds in the, trees, .]", instances.toString());
//        instances = aligner.tokenIndex.getBlockInstancesForWitness(w[1]);
//        Assert.assertEquals("[the cat, birds in the, little, trees, this morning, observed, .]", instances.toString());

        Set<Island> islands = aligner.getIslands();
        assertIslandAsVectorEquals(0, 2, 2, islands); // the cat
        assertIslandAsVectorEquals(4, 6, 3, islands); // birds in the
        assertIslandAsVectorEquals(7, 5, 1, islands); // little
        assertIslandAsVectorEquals(8, 9, 1, islands); // trees
        assertIslandAsVectorEquals(9, 0, 2, islands); // this morning
        assertIslandAsVectorEquals(13, 4, 1, islands); // observed
        assertIslandAsVectorEquals(18, 10, 1, islands); // .
        // When optimised it can be done with 7 islands instead 16
        // non selected islands get modified during alignment into an extra (empty) island
        // Assert.assertEquals(16, islands.size());

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

    @Test
    public void testHermansOverreachTest() {
        final SimpleWitness[] w = createWitnesses("a b c d F g h i ! K ! q r s t", "a b c d F g h i ! q r s t", "a b c d E g h i ! q r s t");
        Dekker21Aligner aligner = new Dekker21Aligner();
        VariantGraph graph = new VariantGraph();
        List<SimpleWitness> witnesses = new ArrayList<>();
        witnesses.addAll(Arrays.asList(w));
        aligner.collate(graph, witnesses);
////        List<Block> blocks = aligner.tokenIndex.getNonOverlappingBlocks();
////        Set<String> blocksAsString = new HashSet<>();
////        blocks.stream().map(interval -> interval.getNormalizedForm()).forEach(blocksAsString::add);
////        Set<String> expected = new HashSet<>(Arrays.asList("birds in the", "the cat", "this morning", ".", "little", "observed", "trees"));
////        Assert.assertEquals(expected, blocksAsString);
//        List<Block.Instance> instances = aligner.tokenIndex.getBlockInstancesForWitness(w[0]);
//        Assert.assertEquals("[a b c d, F, g h i !, K !, q r s t]", instances.toString());
//        instances = aligner.tokenIndex.getBlockInstancesForWitness(w[1]);
//        Assert.assertEquals("[the cat, birds in the, little, trees, this morning, observed, .]", instances.toString());
            //TODO: add asserts!

    }

    @Test
    public void testCaseVariantGraphThreeWitnesses() {
        final SimpleWitness[] w = createWitnesses("The quick brown fox jumps over the lazy dog", "The fast brown fox jumps over the black dog", "The red fox jumps over the fence");
        Dekker21Aligner aligner = new Dekker21Aligner();
        VariantGraph graph = new VariantGraph();
        aligner.collate(graph, w);

        Assert.assertThat(graph, VariantGraphMatcher.graph(w[0]).aligned("the").non_aligned("quick").aligned("brown", "fox", "jumps", "over", "the").non_aligned("lazy").aligned("dog"));
        Assert.assertThat(graph, VariantGraphMatcher.graph(w[1]).aligned("the").non_aligned("fast").aligned("brown", "fox", "jumps", "over", "the").non_aligned("black").aligned("dog"));
        Assert.assertThat(graph, VariantGraphMatcher.graph(w[2]).aligned("the").non_aligned("red").aligned("fox", "jumps", "over", "the").non_aligned("fence"));
    }

    @Test
    public void test3dMatching1() {
        SimpleWitness[] witnesses = createWitnesses("a", "b", "c", "a b c");
        Dekker21Aligner aligner = new Dekker21Aligner();
        VariantGraph graph = new VariantGraph();
        aligner.collate(graph, witnesses);
        Assert.assertThat(graph, VariantGraphMatcher.graph(witnesses[3]).aligned("a", "b", "c"));
    }

    @Test
    public void testCaseVariantGraphTwoDifferentWitnesses() {
        final SimpleWitness[] w = createWitnesses("The quick brown fox jumps over the lazy dog", "The fast brown fox jumps over the black dog");
        Dekker21Aligner aligner = new Dekker21Aligner();
        VariantGraph graph = new VariantGraph();
        aligner.collate(graph, w);

        Assert.assertThat(graph, VariantGraphMatcher.graph(w[0]).aligned("the").non_aligned("quick").aligned("brown", "fox", "jumps", "over", "the").non_aligned("lazy").aligned("dog"));
        Assert.assertThat(graph, VariantGraphMatcher.graph(w[1]).aligned("the").non_aligned("fast").aligned("brown", "fox", "jumps", "over", "the").non_aligned("black").aligned("dog"));
    }

    @Test
    public void testMergeFirstWitness() {
        final SimpleWitness[] w = createWitnesses("The same stuff");
        Dekker21Aligner aligner = new Dekker21Aligner();
        VariantGraph g = new VariantGraph();
        // we collate the first witness --> is a simple add
        aligner.collate(g, w);
        VariantGraph.Vertex[] vertices = aligner.vertex_array;
        assertVertexEquals("the", vertices[0]);
        assertVertexEquals("same", vertices[1]);
        assertVertexEquals("stuff", vertices[2]);
    }

    @Test
    public void testTwoEqualWitnesses() {
        final SimpleWitness[] w = createWitnesses("The same stuff", "The same stuff");
        Dekker21Aligner aligner = new Dekker21Aligner();
        VariantGraph graph = new VariantGraph();
        aligner.collate(graph, w);
        Assert.assertThat(graph, VariantGraphMatcher.graph(w[0]).aligned("the", "same", "stuff"));
        Assert.assertThat(graph, VariantGraphMatcher.graph(w[1]).aligned("the", "same", "stuff"));
    }

    @Test
    public void testCaseTwoWitnessesReplacement() {
        final SimpleWitness[] w = createWitnesses("The black cat", "The red cat");
        Dekker21Aligner aligner = new Dekker21Aligner();
        VariantGraph g = new VariantGraph();
        aligner.collate(g, w);
        Assert.assertThat(g, VariantGraphMatcher.graph(w[0]).aligned("the").non_aligned("black").aligned("cat"));
        Assert.assertThat(g, VariantGraphMatcher.graph(w[1]).aligned("the").non_aligned("red").aligned("cat"));
    }
}