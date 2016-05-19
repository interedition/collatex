package eu.interedition.collatex.subst;

import org.junit.Test;

import java.util.stream.Stream;

/**
 * Created by ronalddekker on 30/04/16.
 */


public class WitnessNodeTest {

    @Test
    public void test_tokenizer1() {
        String xml_in = "<wit n=\"1\"><subst><del>In</del><add>At</add></subst> the <subst><del>beginning</del><add>outset</add></subst>, finding the <subst><del>correct</del><add>right</add></subst> word.</wit>";

        WitnessNode root = WitnessNode.createTree(xml_in);

        Stream<WitnessNode> witnessNodes = root.depthFirstNodeStream();

        //witnessNodes.forEach(System.out::println);

        Stream<WitnessNode.WitnessNodeEvent> witnessEventNodes = root.depthFirstNodeEventStream();

        witnessEventNodes.forEach(System.out::println);
    }
}