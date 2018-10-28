package eu.interedition.collatex.skipgrams;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleToken;

import java.util.*;

/*
 * @author: Ronald Haentjens Dekker
 * Date: 25-10-2018
 */


/* Version 3
 * We create we a list of nodes in an order that is similar to the topological sort of the nodes of the variant
 * graph
 *
 * Insertion of a node
 * Input: We have a token (with a witness identifier and a position in the witness)
 * and the token of has a normalized form.
 *
 * Algorithmic steps:
 * 1. Filter the nodes pf the list on the witness identifier..
 * 2. Navigate the existing nodes using a comparator, not an identifier but on position
 * 3. Find the node a bit lower and the node a bit higher
 * 4. Look at the nodes in between the lower and the higher using the normalized form.
 * 5. If not a normalized form match.. insert a new node
 *      This happens if a witness is th first to add a normalized form thereby creating a new column
 *      or in case of a transposition multiple nodes need to be created.
 * 6. If yes add token to that node
 */

/*
 * Version 2 of the variant graph builder based on skipgrams
 * The idea here is to first create a list of tokens in what will be the topological sort of the nodes
 * of the variant graph.
 *
 * After the list of tokens is created in the right order nodes can be created from the list
 * bij deduplicating the tokens.. then the edges can be created... Sounds like a plan.
    *
    * We use a single navigableMap (either a treemap or skiplist) to accomplish this goal
    *
    *
    *
    *
    *

*/


public class VariantGraphCreator {
    private List<SimpleToken> tokenListInTopologicalOrder = new ArrayList<>();





    /*
    * We need a a key object that is comparable that si a compsoite of a witness id and a comparble token
    * so we create an object for it..
    * Not a key any more just a list..
    */

    public static class TokenComparatorThatAcceptsTokensFromMultipleWitnesses implements Comparator<SimpleToken> {
        // first we check whether the tokens are of the same witness
        // if not, we return -1.
        // If there are from the same witness we call the compare function on the SimpleToken class itself.
        // pretty simple concept.
        @Override
        public int compare(SimpleToken o1, SimpleToken o2) {
            int result = o1.getWitness().getSigil().compareTo(o2.getWitness().getSigil());
            if (result != 0) return result;
            return o1.compareTo(o2);
        }
    }


    public void selectSkipgram(Skipgram skipgram) {
        // I want an ordered list
        // heeft aan array list een comparator?
        // Use: collections.sort
        // Add the head and tail tokens of the skipgram to the list
        // sort the list, using the right comparator and TADA
        SimpleToken head = (SimpleToken) skipgram.head;
        SimpleToken tail = (SimpleToken) skipgram.tail;
        tokenListInTopologicalOrder.add(head);
        tokenListInTopologicalOrder.add(tail);
        tokenListInTopologicalOrder.sort(new TokenComparatorThatAcceptsTokensFromMultipleWitnesses());
    }

    @Override
    public String toString() {
        return tokenListInTopologicalOrder.toString();
    }
}



