package eu.interedition.collatex.dekker.fusiongraph;

import eu.interedition.collatex.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by Ronald Haentjens Dekker on 06/09/2017.
 */
class FusionGraph {
    protected static final int INCOMING = 0;
    protected static final int OUTGOING = 1;
    protected static final int INCIDENT = 2;

    // for now I treat a fusion graph as a sparse graph

    // A Fusion graph contains nodes that represent matches
    final List<FusionNode> nodes;

    // A Fusion graph contains directed and undirected edges that represent relationships between the nodes.
    // Map of vertices to adjacency maps of vertices to {incoming, outgoing, incident} edges
    final Map<FusionNode, Map<FusionNode,FusionEdge>[]> vertexMaps;

    FusionGraph() {
        this.nodes = new ArrayList<>();
        this.vertexMaps = new HashMap<>();
    }

    public void addNode(FusionNode node) {
        this.nodes.add(node);
    }

    //TODO
//    public void addDirectedEdge(FusionEdge edge, FusionNode source, FusionNode target) {
//
//    }
}

// Nodes have two tokens on them; there is no need to represent the token sequences as graphs themselves.
// Maybe nodes should also know where they are positioned?
// Should nodes know their edges? For now I can put the edges on the graph
class FusionNode {
    Token tokenA;
    private Token tokenB;
    private int x;
    private int y;

    FusionNode(Token tokenA, Token tokenB) {
        this.tokenA = tokenA;
        this.tokenB = tokenB;
    }

    @Override
    public String toString() {
        return "fn:"+tokenA.toString();
    }
}

class FusionEdge {
    final EdgeType type;
    public FusionEdge(EdgeType type) {
        this.type = type;
    }
}

enum EdgeType {
    DIRECTED,
    UNDIRECTED
}