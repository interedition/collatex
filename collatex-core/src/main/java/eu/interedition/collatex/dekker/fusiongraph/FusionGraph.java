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
    private static final int INCOMING = 0;
    private static final int OUTGOING = 1;
    protected static final int INCIDENT = 2;

    // for now I treat a fusion graph as a sparse graph

    // A Fusion graph contains nodes that represent matches
    final List<FusionNode> nodes;

    // A Fusion graph contains directed and undirected edges that represent relationships between the nodes.
    // Map of vertices to adjacency maps of vertices to {incoming, outgoing, incident} edges
    // I don't need a separate Fusion Edge object for the moment, because we cannot create double edges
    // between vertices.
    private final Map<FusionNode, FusionNode[]> vertexMaps;

    FusionGraph() {
        this.nodes = new ArrayList<>();
        this.vertexMaps = new HashMap<>();
    }

    void addNode(FusionNode node) {
        this.nodes.add(node);
    }

    void addDirectedEdge(FusionNode source, FusionNode target) {
        // every edge needs to be added twice to the vertex maps; no matter the type
        vertexMaps.computeIfAbsent(source, node -> new FusionNode[3])[OUTGOING] = target;
        vertexMaps.computeIfAbsent(target, node -> new FusionNode[3])[INCOMING] = source;
    }
}

// Nodes have two tokens on them; there is no need to represent the token sequences as graphs themselves.
// Maybe nodes should also know where they are positioned?
// Should nodes know their edges? For now I can put the edges on the graph
class FusionNode {
    Token tokenA;
    private Token tokenB;
    private int x;
    int y;

    FusionNode(Token tokenA, Token tokenB, int x, int y) {
        this.tokenA = tokenA;
        this.tokenB = tokenB;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "fn:"+tokenA.toString()+" "+x+" "+y;
    }
}

