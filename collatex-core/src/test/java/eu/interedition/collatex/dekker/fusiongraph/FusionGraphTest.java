package eu.interedition.collatex.dekker.fusiongraph;

import com.google.common.truth.Correspondence;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.dekker.token_index.TokenIndex;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import javax.annotation.Nullable;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

class FakeNode {
    private final int expectedX;
    final int expectedY;
    String content;

    FakeNode(String content, int expectedX, int expectedY) {
        this.content = content;
        this.expectedX = expectedX;
        this.expectedY = expectedY;
    }

    @Override
    public String toString() {
        return "fa:"+content;
    }
}

class FusionNodeCorrespondence extends Correspondence<FusionNode, FakeNode> {
    @Override
    public boolean compare(@Nullable FusionNode fusionNode, @Nullable FakeNode fakeNode) {
        boolean b = fusionNode != null && fakeNode != null;
        if (!b) return false;
        //if (equals) System.out.println("Comparing "+fusionNode.tokenA +" and "+fakeNode.content);
        return ((SimpleToken) fusionNode.tokenA).getNormalized().equals(fakeNode.content) && fakeNode.expectedY == fusionNode.y;
    }

    @Override
    public String toString() {
        return "DON'T KNOW WHAT TO PUT HERE!";
    }
}


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

        FusionGraphBuilder builder = new FusionGraphBuilder();
        FusionGraph graph = builder.createFusionGraph(w, tokenIndex);
        assertEquals(6, graph.nodes.size());

        assertThat(graph.nodes)
            .comparingElementsUsing(new FusionNodeCorrespondence())
            .containsExactly(new FakeNode("p", 1, 1), new FakeNode("s", 3, 3), new FakeNode("f", 4, 4), new FakeNode("t", 5, 5), new FakeNode("t", 9, 5), new FakeNode("y", 6, 6));
    }
}
