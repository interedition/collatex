package eu.interedition.collatex.dekker.order_independent_mwa;

import org.junit.Test;

/**
 * Created by Ronald Dekker on 08/07/17.
 */
public class TestSituation {

    @Test
    public void testMultiplePathsThroughAlignmentTable() {
        /* The idea is to do mwa using a global scoring based on a histogram
        * I did work on that approach in December of 2016
        * - Extending the NeedlemannWunsch with multiple witnesses without progressive alignment would yield too
        * many combinations at each node of the edit graph.
        * - Trying all the possible orders in progressive multiple witness alignment also does not work,
        * you run into the travelling salesman problem.
        * - What we will try to do instead is to determine whether there are multiple paths
        * through the edit graph that all have to highest (= best) score possible.
        * Then we create a variant graph for each of the paths through the graph
        * We can create a decision tree for each of them and calculate the best outcome.
        */

    }
}
