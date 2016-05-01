package eu.interedition.collatex.subst;

import eu.interedition.collatex.Witness;
import org.junit.Test;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by ronalddekker on 30/04/16.
 */


public class WitnessTreeTest {

    @Test
    public void test_tokenizer1() {
        String xml_in = "<wit n=\"1\"><subst><del>In</del><add>At</add></subst> the <subst><del>beginning</del><add>outset</add></subst>, finding the <subst><del>correct</del><add>right</add></subst> word.</wit>";

        WitnessTree.WitnessNode root = WitnessTree.createTree(xml_in);

        Stream<WitnessTree.WitnessNode> witnessNodes = root.depthFirstNodeStream();

        //witnessNodes.forEach(System.out::println);

        Stream<WitnessTree.WitnessNodeEvent> witnessEventNodes = root.depthFirstNodeEventStream();

        witnessEventNodes.forEach(System.out::println);
    }
}