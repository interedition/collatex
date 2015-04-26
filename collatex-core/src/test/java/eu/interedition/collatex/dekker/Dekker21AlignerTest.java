package eu.interedition.collatex.dekker;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class Dekker21AlignerTest extends AbstractTest {

    @Test
    public void testExample1() {
//        VariantGraph against = new VariantGraph();
//        aligner.collate(against, w[0]);

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
    }
}
