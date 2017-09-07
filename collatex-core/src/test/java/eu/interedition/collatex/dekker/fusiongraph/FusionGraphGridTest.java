package eu.interedition.collatex.dekker.fusiongraph;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.token_index.TokenIndex;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

/**
 *
 * Created by Ronald Haentjens Dekker on 06/09/2017.
 */
public class FusionGraphGridTest extends AbstractTest {

    @Test
    public void testCreationOfAFusionGraphGrid() {
        // first we create three witnesses
        // TODO: make them the witnesses as described in the paper
        final SimpleWitness[] w = createWitnesses("The quick brown fox jumps over the lazy dog", "The fast brown fox jumps over the black dog", "The red fox jumps over the fence");

        // We create a token index based on the witnesses
        TokenIndex tokenIndex = new TokenIndex(new EqualityTokenComparator(), w);

        // get the token array from the token index
        Token[] tokenArray = tokenIndex.token_array;

        // Create the fusion graph grid based on the token array
        /*FusionGraphGrid grid = */ new FusionGraphGrid(tokenArray);

        // the fusion graph grid is a container to store the fusion graph in...
        /*FusionGraph fg = */ new FusionGraph();
    }
}
