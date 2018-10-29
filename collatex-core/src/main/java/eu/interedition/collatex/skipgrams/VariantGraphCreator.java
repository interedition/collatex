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

public class VariantGraphCreator {
    private VariantGraph variantGraph;
    private VariantGraph.Vertex start;
    private VariantGraph.Vertex end;
    private List<VariantGraph.Vertex> verticesListInTopologicalOrder;

    public VariantGraphCreator() {
        this.variantGraph = new VariantGraph();
        this.start = new VariantGraph.Vertex(this.variantGraph);
        this.end = new VariantGraph.Vertex(this.variantGraph);
        this.verticesListInTopologicalOrder = new ArrayList<>();
        this.verticesListInTopologicalOrder.add(start);
        this.verticesListInTopologicalOrder.add(end);
    }







    public void selectSkipgram(Skipgram skipgram) {
        SimpleToken head = (SimpleToken) skipgram.head;
        SimpleToken tail = (SimpleToken) skipgram.tail;

        // we must look for the location where to insert the vertex
        // this method has to be called twice. Once for each token in the skipgram
        insertTokenInVariantGraph(head);
        System.out.println(this.toString());
        insertTokenInVariantGraph(tail);
    }

    public void insertTokenInVariantGraph(SimpleToken token) {
        // This method should return two vertices: one that is higher than the one we want to insert
        // and one that is lower.
        VariantGraph.Vertex lower = start;
        // maybe use optional?
        VariantGraph.Vertex higher = null;
        for (VariantGraph.Vertex v : verticesListInTopologicalOrder) {
            // If V does not contain the witnss that w are looking for then ski[
            if (v == start) {
                continue;
            }
            if (v == end) {
                System.out.println("The end node is higher than "+token.toString());
                higher = v;
                break;
            }
            // Do the actual token comparison
            String witnessId = token.getWitness().getSigil();
            // search the other token
            SimpleToken theOtherToken = (SimpleToken) v.tokens().stream().filter(p -> p.getWitness().getSigil().equals(witnessId)).findFirst().get();
            System.out.println("Comparing other "+ theOtherToken+" and "+token+"!");
            int bla = theOtherToken.compareTo(token);
            System.out.println("outcome:"+bla);

            if (bla < 0) {
                System.out.println("token "+token+" is higher than "+theOtherToken);
                lower = v;
                continue;
            }
            if (bla > 0) {
                //TODO!
                higher = v;
            }
        }
        // for now we always create a new vertex per token.
        // This is of course wrong.
        // TODO: search to see whether we need to create a new vertex or not
        VariantGraph.Vertex vertex = new VariantGraph.Vertex(variantGraph);
        vertex.tokens().add(token);
        // integer position of lower berekenen.
        int i = verticesListInTopologicalOrder.indexOf(lower);
        verticesListInTopologicalOrder.add(i+1, vertex);
    }





    @Override
    public String toString() {
        return verticesListInTopologicalOrder.toString();
    }
}





/*
 * We need a node comparator
 * that after checking the witness identifier overlap ( de smallest set has to be present in the fuller set)
 * delegates to a token comparator based on position to do the rest. Simple Token Comparator has that.
 *
 * We could create a node witness view.
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



//    /*
//    * We need a a key object that is comparable that si a compsoite of a witness id and a comparble token
//    * so we create an object for it..
//    * Not a key any more just a list..
//    */
//
//    public static class TokenComparatorThatAcceptsTokensFromMultipleWitnesses implements Comparator<SimpleToken> {
//        // first we check whether the tokens are of the same witness
//        // if not, we return -1.
//        // If there are from the same witness we call the compare function on the SimpleToken class itself.
//        // pretty simple concept.
//        @Override
//        public int compare(SimpleToken o1, SimpleToken o2) {
//            int result = o1.getWitness().getSigil().compareTo(o2.getWitness().getSigil());
//            if (result != 0) return result;
//            return o1.compareTo(o2);
//        }
//    }
