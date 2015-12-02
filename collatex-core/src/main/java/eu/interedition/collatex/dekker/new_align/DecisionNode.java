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
    private final DecisionNode parent;
    private final DecisionTree tree;
    private final List<Island> selected;
    private final List<Island> moved;

    public DecisionNode(DecisionTree tree) {
        this.positionGraph = 0;
        this.positionWitness = 0;
        this.tree = tree;
        this.parent = null;
        // Selected phrasematch should only be one
        selected = new ArrayList<>();
        moved = new ArrayList<>();
    }

    public DecisionNode(DecisionNode parent) {
        // Set positions on the child node to the positions of the parent node and calculate from there
        this.positionGraph = parent.positionGraph;
        this.positionWitness = parent.positionWitness;
        this.tree = parent.tree;
        this.parent = parent;
        // Selected phrasematch should only be one
        selected = new ArrayList<>();
        moved = new ArrayList<>();
    }

    public Island getGraphPhrase() {
        return tree.getIslandOnGraphPosition(positionGraph);
    }

    public Island getWitnessPhrase() {
        return tree.getIslandOnWitnessPosition(positionWitness);
    }

    public List<Island> getSelected() {
        return selected;
    }

    public List<Island> getMoved() {
        return moved;
    }

    @Override
    public String toString() {
        return getGraphPhrase()+"; "+getWitnessPhrase();
    }

    //TODO: should this method be public? I don't think so.
    public void select(Island selectWitnessPhraseMatch) {
        System.out.println("selected: "+selectWitnessPhraseMatch);
        selected.add(selectWitnessPhraseMatch);
    }

    private void move(Island graphPhraseMatch) {
        System.out.println("considered as moved: "+graphPhraseMatch);
        moved.add(graphPhraseMatch);
    }


    //TODO: this should eventually become a public method
    //NOTE: This stuff is still in an experimental state
    protected DecisionNode getDecisionNodeChildForWitnessPhrase() {
        DecisionNode child2 = new DecisionNode(this);
        // move all the phrase matches before the selected phrase match
        Island selectWitnessPhraseMatch = tree.getIslandOnWitnessPosition(child2.positionWitness);
        // now find the position of the linked match in the other array
        //selectWitnessPhraseMatch.
        Island graphPhraseMatch = tree.getIslandOnGraphPosition(child2.positionGraph);
        while(graphPhraseMatch != selectWitnessPhraseMatch) {
            //TODO: check whether item is already occupied
            child2.move(graphPhraseMatch);
            child2.positionGraph++;
            graphPhraseMatch = tree.getIslandOnGraphPosition(child2.positionGraph);
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
        return child2;
    }

    private void skipToNextAvailableGraph() {
        // now we need to skip elements that are not available anymore
        // we can do this the ugly way
        // transform the moved and the selected phrases into fixed bits for the witness positions and fixed vertices for the graph positions.
        FillAreaCovered fillAreaCovered = new FillAreaCovered().invoke();
        Set<VariantGraph.Vertex> vertices = fillAreaCovered.getVertices();
        BitSet positions = fillAreaCovered.getPositions();

        // check next phrase on graph order
        Island graphPhrase = getGraphPhrase();
//        System.out.println("testing: "+graphPhrase);
        //  check whether phrase is available
        //TODO: this check is too simple
        //if the first vertex and token are available it does not mean that the complete phrase
        //is available
        while (vertices.contains(graphPhrase.getMatch(0).vertex) || positions.get(graphPhrase.getLeftEnd().row)) {
            // skip graph phrase
//            System.out.println("skipped: "+graphPhrase);
            positionGraph++;
            graphPhrase = getGraphPhrase();
        }
    }

    private void skipToNextAvailableWitness() {
        // now we need to skip elements that are not available anymore
        // we can do this the ugly way
        // transform the moved and the selected phrases into fixed bits for the witness positions and fixed vertices for the graph positions.
        FillAreaCovered fillAreaCovered = new FillAreaCovered().invoke();
        Set<VariantGraph.Vertex> vertices = fillAreaCovered.getVertices();
        BitSet positions = fillAreaCovered.getPositions();

        // check next phrase on witness order
        Island witnessPhrase = getWitnessPhrase();
//        System.out.println("testing: "+witnessPhrase);
        //  check whether phrase is available
        //TODO: this check is too simple
        //if the first vertex and token are available it does not mean that the complete phrase
        //is available
        while (vertices.contains(witnessPhrase.getMatch(0).vertex) || positions.get(witnessPhrase.getLeftEnd().row)) {
            // skip witness phrase
//            System.out.println("skipped: "+witnessPhrase);
            positionWitness++;
            witnessPhrase = getWitnessPhrase();
        }
    }

    private class FillAreaCovered {
        private Set<VariantGraph.Vertex> vertices;
        private BitSet positions;

        public Set<VariantGraph.Vertex> getVertices() {
            return vertices;
        }

        public BitSet getPositions() {
            return positions;
        }

        public FillAreaCovered invoke() {
            // now we need to skip elements that are not available anymore
            // we can do this the ugly way
            // transform the moved and the selected phrases into fixed bits for the witness positions and fixed vertices for the graph positions.
            vertices = new HashSet<>();
            positions = new BitSet();

            DecisionNode current = DecisionNode.this;
            do {
                convert(vertices, positions, current.moved);
                convert(vertices, positions, current.selected);
                // System.out.println(vertices);
                // System.out.println(positions);
                current = current.parent;
            }  while (current!=null);
            return this;
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
