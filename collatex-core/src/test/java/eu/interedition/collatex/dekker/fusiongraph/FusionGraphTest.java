package eu.interedition.collatex.dekker.fusiongraph;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.dekker.token_index.Block;
import eu.interedition.collatex.dekker.token_index.TokenIndex;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import java.util.*;

public class FusionGraphTest extends AbstractTest {

    @Test
    public void testCreationOfAFusionGraph() {
        // first we create three witnesses
        String w1 = "P H S F T Y V M T";
        String w2 = "P G S F T Y W";
        String w3 = "R F T G F W";
        SimpleWitness[] w = createWitnesses(w1, w2, w3);

        // We create a token index based on the witnesses
        TokenIndex tokenIndex = new TokenIndex(new EqualityTokenComparator(), w);
        // this prepare method should go!
        tokenIndex.prepare();

        // create an empty fusion graph
        // FusionGraph fg = new FusionGraph();

        // we need an algorithm to convert a token index to nodes positioned in the fusion graph.

        // we need to align all the witnesses to each other.
        // so that is 1 to 2 (where the 1 is on the y axis and the 2 is on the x axis)
        // and 1 to 3
        // and 2 to 3

        // we start with 1 to 2

        // There is a method to get all block instances for a witness
        // But that is not really what we want..
        // We want all the blocks that are both in one witness and in the other witness.
        // we map all the block instances for one witness to a block
        Map<Block, Block.Instance> map1 = new HashMap<>();
        List<Block.Instance> instancesForFirstWitness = tokenIndex.getBlockInstancesForWitness(w[0]);
        for (Block.Instance i : instancesForFirstWitness) {
            Block block = i.getBlock();
            map1.put(block, i);
        }

        Map<Block, Block.Instance> map2 = new HashMap<>();
        List<Block.Instance> instancesForSecondWitness = tokenIndex.getBlockInstancesForWitness(w[1]);
        for (Block.Instance i : instancesForSecondWitness) {
            Block block = i.getBlock();
            map2.put(block, i);
        }

        Set<Block> blocksForBothWitnesses = new HashSet<>();
        blocksForBothWitnesses.addAll(map1.keySet());
        blocksForBothWitnesses.retainAll(map2.keySet());

        // This will be the nodes, there are some duplicates, because the current token index finds
        // the largest blocks regarding width and depth.
        Set<Integer> alreadyDone = new HashSet<>();
        for (Block b: blocksForBothWitnesses) {
            Block.Instance instance = map1.get(b);
            if (!alreadyDone.contains(instance.start_token)) {
                alreadyDone.add(instance.start_token);
                Block.Instance instance2 = map2.get(b);
                System.out.println(instance.getTokens().get(0)+";"+instance2.getTokens().get(0));
            }
        }
    }

}
