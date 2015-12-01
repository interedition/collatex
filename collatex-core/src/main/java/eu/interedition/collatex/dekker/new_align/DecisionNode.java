package eu.interedition.collatex.dekker.new_align;

import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.island.Island;

import java.util.*;

/**
 * Created by ronalddekker on 23/11/15.
 */
public class DecisionNode {

    int positionGraph;
    int positionWitness;
    private final DecisionTree tree;
    private final List<Island> selected;
    private final List<Island> moved;

    public DecisionNode(DecisionTree tree) {
        this.positionGraph = 0;
        this.positionWitness = 0;
        this.tree = tree;
        selected = new ArrayList<>();
        moved = new ArrayList<>();
    }

    public Island getGraphPhrase() {
        return tree.getIslandOnGraphPosition(positionGraph);
    }

    public Island getWitnessPhrase() {
        return tree.getIslandOnWitnessPosition(positionWitness);
    }

    public void select(Island selectWitnessPhraseMatch) {
        System.out.println("selected: "+selectWitnessPhraseMatch);
        selected.add(selectWitnessPhraseMatch);
    }

    private void move(Island graphPhraseMatch) {
        System.out.println("considered as moved: "+graphPhraseMatch);
        moved.add(graphPhraseMatch);
    }



    //NOTE: This stuff is still in an experimental state
    protected DecisionNode getDecisionNodeChildForWitnessPhrase(DecisionTree decisionTree) {
        DecisionNode child2 = new DecisionNode(decisionTree);
        //child2.positionWitness++; // select the witness phrase match ("the cat")
        //TODO: set positions


        // move all the phrase matches before the selected phrase match
        Island selectWitnessPhraseMatch = decisionTree.getIslandOnWitnessPosition(child2.positionWitness);
        // now find the position of the linked match in the other array
        //selectWitnessPhraseMatch.
        Island graphPhraseMatch = decisionTree.getIslandOnGraphPosition(child2.positionGraph);
        while(graphPhraseMatch != selectWitnessPhraseMatch) {
            //TODO: check whether item is already occupied
            child2.move(graphPhraseMatch);
            child2.positionGraph++;
            graphPhraseMatch = decisionTree.getIslandOnGraphPosition(child2.positionGraph);
        }
        // select witness phrase match
        child2.select(selectWitnessPhraseMatch);
        // move the pointer further till the next available phrase match
        // wwe have to keep track of the selected vertices and selected tokens to test this
        // for now we can do this simply by moving the pointer by the length of selected
        child2.positionWitness += selectWitnessPhraseMatch.size();
        // the pointers of both positions should be moved
        child2.positionGraph += selectWitnessPhraseMatch.size();
        child2.skipToNextAvailableGraph();
        child2.skipToNextAvailableWitness();

        //TODO; check next phrase on witness order
        return child2;
    }

    private void skipToNextAvailableGraph() {
        // now we need to skip elements that are not available anymore
        // we can do this the ugly way
        // transform the moved and the selected phrases into fixed bits for the witness positions and fixed vertices for the graph positions.
        Set<VariantGraph.Vertex> vertices = new HashSet<>();
        BitSet positions = new BitSet();
        convert(vertices, positions, moved);
        convert(vertices, positions, selected);
        System.out.println(vertices);
        System.out.println(positions);
        // check next phrase on graph order
        Island graphPhrase = getGraphPhrase();
        System.out.println("testing: "+graphPhrase);
        //  check whether phrase is available
        //TODO: this check is too simple
        //if the first vertex and token are available it does not mean that the complete phrase
        //is available
        while (vertices.contains(graphPhrase.getMatch(0).vertex) || positions.get(graphPhrase.getLeftEnd().row)) {
            // skip graph phrase
            System.out.println("skipped: "+graphPhrase);
            positionGraph++;
            graphPhrase = getGraphPhrase();
        }
    }

    private void skipToNextAvailableWitness() {
        // now we need to skip elements that are not available anymore
        // we can do this the ugly way
        // transform the moved and the selected phrases into fixed bits for the witness positions and fixed vertices for the graph positions.
        Set<VariantGraph.Vertex> vertices = new HashSet<>();
        BitSet positions = new BitSet();
        convert(vertices, positions, moved);
        convert(vertices, positions, selected);
        System.out.println(vertices);
        System.out.println(positions);
        // check next phrase on witness order
        Island witnessPhrase = getWitnessPhrase();
        System.out.println("testing: "+witnessPhrase);
        //  check whether phrase is available
        //TODO: this check is too simple
        //if the first vertex and token are available it does not mean that the complete phrase
        //is available
        while (vertices.contains(witnessPhrase.getMatch(0).vertex) || positions.get(witnessPhrase.getLeftEnd().row)) {
            // skip witness phrase
            System.out.println("skipped: "+witnessPhrase);
            positionWitness++;
            witnessPhrase = getWitnessPhrase();
        }
    }

    private static void convert(Set<VariantGraph.Vertex> vertices, BitSet positions, List<Island> phrases) {
        for (Island taken : phrases) {
            for (int i=0; i < taken.size(); i++) {
                // Just storing and testing against the matches is not good enough
                // matches can overlap with each other..
                // storing the rank instead of the vertex is also not good enough
                // there can be multiple vertices on the same rank
                // locking one vertex, does not lock the other(s)
                vertices.add(taken.getMatch(i).vertex);
                positions.set(taken.getLeftEnd().row+i);
            }
        }
    }

}
