package eu.interedition.collatex.dekker;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.island.Coordinate;
import eu.interedition.collatex.dekker.island.Island;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static eu.interedition.collatex.dekker.token_index.VariantGraphMatcher.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DekkerAlgorithmTest extends AbstractTest {

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
        DekkerAlgorithm aligner = new DekkerAlgorithm();
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

        Set<Island> islands = aligner.getAllPossibleIslands();
        assertIslandAsVectorEquals(0, 2, 2, islands); // the cat
        assertIslandAsVectorEquals(4, 6, 3, islands); // birds in the
        assertIslandAsVectorEquals(7, 5, 1, islands); // little
        assertIslandAsVectorEquals(8, 9, 1, islands); // trees
        assertIslandAsVectorEquals(9, 0, 2, islands); // this morning
        assertIslandAsVectorEquals(13, 4, 1, islands); // observed
        assertIslandAsVectorEquals(18, 10, 1, islands); // .
        assertEquals(16, islands.size());

        List<Island> selectedIslands = aligner.getPreferredIslands();
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

        assertThat(graph, graph(w[0]).non_aligned("this", "morning").aligned("the", "cat").non_aligned("observed").non_aligned("little").aligned("birds", "in", "the").aligned("trees", "."));
        assertThat(graph, graph(w[1]).aligned("the", "cat").non_aligned("was", "observing").aligned("birds", "in", "the").non_aligned("little").aligned("trees").non_aligned("this", "morning").non_aligned(",", "it").non_aligned("observed", "birds", "for", "two", "hours").aligned("."));
    }

    @Test
    public void testCaseVariantGraphThreeWitnesses() {
        final SimpleWitness[] w = createWitnesses("The quick brown fox jumps over the lazy dog", "The fast brown fox jumps over the black dog", "The red fox jumps over the fence");
        DekkerAlgorithm aligner = new DekkerAlgorithm();
        VariantGraph graph = new VariantGraph();
        aligner.collate(graph, w);

        assertThat(graph, graph(w[0]).aligned("the").non_aligned("quick").aligned("brown", "fox", "jumps", "over", "the").non_aligned("lazy").aligned("dog"));
        assertThat(graph, graph(w[1]).aligned("the").non_aligned("fast").aligned("brown", "fox", "jumps", "over", "the").non_aligned("black").aligned("dog"));
        assertThat(graph, graph(w[2]).aligned("the").non_aligned("red").aligned("fox", "jumps", "over", "the").non_aligned("fence"));
    }

    @Test
    public void test3dMatching1() {
        SimpleWitness[] witnesses = createWitnesses("a", "b", "c", "a b c");
        DekkerAlgorithm aligner = new DekkerAlgorithm();
        VariantGraph graph = new VariantGraph();
        aligner.collate(graph, witnesses);
        assertThat(graph, graph(witnesses[3]).aligned("a", "b", "c"));
    }

    @Test
    public void testCaseVariantGraphTwoDifferentWitnesses() {
        final SimpleWitness[] w = createWitnesses("The quick brown fox jumps over the lazy dog", "The fast brown fox jumps over the black dog");
        DekkerAlgorithm aligner = new DekkerAlgorithm();
        VariantGraph graph = new VariantGraph();
        aligner.collate(graph, w);

        assertThat(graph, graph(w[0]).aligned("the").non_aligned("quick").aligned("brown", "fox", "jumps", "over", "the").non_aligned("lazy").aligned("dog"));
        assertThat(graph, graph(w[1]).aligned("the").non_aligned("fast").aligned("brown", "fox", "jumps", "over", "the").non_aligned("black").aligned("dog"));
    }

    @Test
    public void testMergeFirstWitness() {
        final SimpleWitness[] w = createWitnesses("The same stuff");
        DekkerAlgorithm aligner = new DekkerAlgorithm();
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
        DekkerAlgorithm aligner = new DekkerAlgorithm();
        VariantGraph graph = new VariantGraph();
        aligner.collate(graph, w);
        assertThat(graph, graph(w[0]).aligned("the", "same", "stuff"));
        assertThat(graph, graph(w[1]).aligned("the", "same", "stuff"));
    }

    @Test
    public void testCaseTwoWitnessesReplacement() {
        final SimpleWitness[] w = createWitnesses("The black cat", "The red cat");
        DekkerAlgorithm aligner = new DekkerAlgorithm();
        VariantGraph g = new VariantGraph();
        aligner.collate(g, w);
        assertThat(g, graph(w[0]).aligned("the").non_aligned("black").aligned("cat"));
        assertThat(g, graph(w[1]).aligned("the").non_aligned("red").aligned("cat"));
    }

    @Test
    public void testDifficultCase1TranspositionOrTwoReplacements() {
        final SimpleWitness[] w = createWitnesses("the cat and the dog", "the dog and the cat");
        DekkerAlgorithm aligner = new DekkerAlgorithm();
        VariantGraph g = new VariantGraph();
        aligner.collate(g, w);
        assertThat(g, graph(w[0]).aligned("the").non_aligned("cat").aligned("and the").non_aligned("dog"));
        assertThat(g, graph(w[1]).aligned("the").non_aligned("dog").aligned("and the").non_aligned("cat"));
    }

    @Test
    public void testDifficultCase2HermansOverreachTest() {
        final SimpleWitness[] w = createWitnesses("a b c d F g h i ! K ! q r s t", "a b c d F g h i ! q r s t", "a b c d E g h i ! q r s t");
        DekkerAlgorithm aligner = new DekkerAlgorithm();
        VariantGraph graph = new VariantGraph();
        aligner.collate(graph, w);
        assertThat(graph, graph(w[0]).aligned("a b c d f g h i !").non_aligned("k !").aligned("q r s t"));
        assertThat(graph, graph(w[1]).aligned("a b c d f g h i ! q r s t"));
        assertThat(graph, graph(w[2]).aligned("a b c d").non_aligned("e").aligned("g h i ! q r s t"));
    }

    // Depth should be taken into account during transposition phase
    @Ignore
    @Test
    public void testDifficultCase3DepthShouldMatter() {
        // 1: a, b, c, d, e
        // 2: a, e, c, d
        // 3: a, X, X, d, b
        final SimpleWitness[] w = createWitnesses("a b c d e", "a e c d", "a d b");
        DekkerAlgorithm aligner = new DekkerAlgorithm();
        VariantGraph graph = new VariantGraph();
        aligner.collate(graph, w);
        assertThat(graph, graph(w[0]).aligned("a b c d").non_aligned("e"));
        assertThat(graph, graph(w[1]).aligned("a").non_aligned("e").aligned("c d"));
        assertThat(graph, graph(w[2]).aligned("a").aligned("d").non_aligned("b"));
    }

    // NOTE: transpositions are asserted in another test
    @Test
    public void testDifficultCasePartialRightOverlapAndTranspositions() {
        SimpleWitness[] w = createWitnesses("και αποκριθεισ ειπεν αυτω ου βλεπεισ ταυτασ μεγαλασ οικοδομασ αμην λεγω σοι ο(υ μη α)φεθη ωδε λιθοσ επι λιθω (οσ ου) μη καταλυθη", "και αποκριθεισ ο ι̅σ̅ ειπεν αυτω βλεπεισ Ταυτασ τασ μεγαλασ οικοδομασ λεγω υμιν ου μη αφεθη λιθοσ επι λιθου οσ ου μη καταλυθη", "και ο ι̅σ̅ αποκριθεισ ειπεν αυτω βλεπεισ ταυτασ τασ μεγαλασ οικοδομασ ου μη αφεθη λιθοσ επι λιθον οσ ου μη καταλυθη");
        DekkerAlgorithm aligner = new DekkerAlgorithm();
        VariantGraph graph = new VariantGraph();
        aligner.collate(graph, w);
        List<SortedMap<Witness, Set<Token>>> t = table(graph);
        assertEquals("|και| | |αποκριθεισ| | |ειπεν|αυτω|ου|βλεπεισ|ταυτασ| |μεγαλασ|οικοδομασ|αμην|λεγω|σοι|ο(υ|μη|α)φεθη|ωδε|λιθοσ|επι|λιθω|(οσ|ου)|μη|καταλυθη|", toString(t, w[0]));
        assertEquals("|και| | |αποκριθεισ|ο|ι̅σ̅|ειπεν|αυτω| |βλεπεισ|ταυτασ|τασ|μεγαλασ|οικοδομασ| |λεγω|υμιν|ου|μη|αφεθη| |λιθοσ|επι|λιθου|οσ|ου|μη|καταλυθη|", toString(t, w[1]));
        assertEquals("|και|ο|ι̅σ̅|αποκριθεισ| | |ειπεν|αυτω| |βλεπεισ|ταυτασ|τασ|μεγαλασ|οικοδομασ| | | |ου|μη|αφεθη| |λιθοσ|επι|λιθον|οσ|ου|μη|καταλυθη|", toString(t, w[2]));
    }

    @Test
    public void testDifficultCasePartialDarwin() {
        SimpleWitness[] w = createWitnesses("those to which the parent-species have been exposed under nature. There is, also, I think, some probability", "those to which the parent-species have been exposed under nature. There is also, I think, some probability", "those to which the parent-species had been exposed under nature. There is also, I think, some probability", "those to which the parent-species had been exposed under nature. There is, also, some probability");
        DekkerAlgorithm aligner = new DekkerAlgorithm();
        VariantGraph graph = new VariantGraph();
        aligner.collate(graph, w);
        assertThat(graph, graph(w[0]).aligned("those to which the parent-species have been exposed under nature . there is , also , i think , some probability"));
        assertThat(graph, graph(w[1]).aligned("those to which the parent-species have been exposed under nature . there is also , i think , some probability"));
        assertThat(graph, graph(w[2]).aligned("those to which the parent-species had been exposed under nature . there is also , i think , some probability"));
        assertThat(graph, graph(w[3]).aligned("those to which the parent-species had been exposed under nature . there is , ").aligned(4, "also").aligned(", some probability"));
    }
}