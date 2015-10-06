package eu.interedition.collatex.dekker;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.astar.AstarAlgorithm;
import eu.interedition.collatex.dekker.astar.Cost;
import eu.interedition.collatex.dekker.experimental_aligner.Block;
import eu.interedition.collatex.dekker.experimental_aligner.Dekker21Aligner;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.*;

/**
 * Experimental code; not yet ready for production
 * Created by ronald on 4/24/15.
 */
public class DecisionGraphAligner {
    protected Dekker21Aligner.TokenIndex tokenIndex;
    // tokens are mapped to vertices by their position in the token array
    protected VariantGraph.Vertex[] vertex_array;
    private Map<VariantGraph.Vertex, Block> vertexToLCP;


    public void align(VariantGraph against, Iterable<Token> witness) {
        //NOTE: the following method assigns the decision graph to the field decisionGraph
        ThreeDimensionalDecisionGraph decisionGraph = createDecisionGraph(against, witness);

        // Do the actual alignment
        List<ExtendedGraphEdge> edges = decisionGraph.getOptimalPath();
        // from the list of edges we need to extract the matches
        // and construct a map (token -> vertex) containing the alignment.
        Map<Token, VariantGraph.Vertex> alignments = new HashMap<>();
        ExtendedGraphNode previous = decisionGraph.getRoot();
        for (ExtendedGraphEdge edge : edges) {
            ExtendedGraphNode targetNode = decisionGraph.getTarget(edge);
            if (edge.isMatch()) {
                Block lcpInterval = edge.block;
                //NOTE: this does not always have to be true
                //intervals can occur multiple times in one witness
                int tokenPosition = getLowestTokenPosition(lcpInterval);
                for (int i=0; i< lcpInterval.length; i++) {
                    // we need:
                    // 1. token in graph and associated vertex in graph (tokenPosition+i)
                    // 2. token in witness (startRangeWitness+positionIndex+i)
                    VariantGraph.Vertex v = vertex_array[tokenPosition+i];
                    Token token = tokenIndex.token_array.get(decisionGraph.startRangeWitness2+previous.startPosWitness2+i);
                    alignments.put(token, v);
                    //TODO: fill vertex array for current witness
                }
            }
            previous = targetNode;
        }
        merge(against, witness, alignments);
    }

    private void merge(VariantGraph against, Iterable<Token> witness, Map<Token, VariantGraph.Vertex> alignments) {
    }

    public ThreeDimensionalDecisionGraph createDecisionGraph(VariantGraph against, Iterable<Token> witness) {
        int beginWitness2 = tokenIndex.getStartTokenPositionForWitness(witness.iterator().next().getWitness());

        // prepare vertices
        List<VariantGraph.Vertex> vert = copyIterable(against.vertices());
        // remove start / end vertices
        vert.remove(0);
        vert.remove(vert.size() - 1);
        VariantGraph.Vertex[] vertices = vert.toArray(new VariantGraph.Vertex[vert.size()]);

        // prepare tokens
        List<Token> tok = copyIterable(witness);
        Token[] tokens = tok.toArray(new Token[tok.size()]);

        // Align using decisionGraph algorithm in 2D
        // first dimension: vertices in topological order
        // second dimension: witness tokens
        // TODO: This should be done in a 3D space. 2D causes artifacts, since there are multiple topological sorts of the graph.
        // TODO: first dimension: vertex ranks
        // TODO: second dimension: all vertices of a certain rank
        // TODO: third dimension: witness tokens

        VariantGraphRanking ranking = VariantGraphRanking.of(against);
        ThreeDimensionalDecisionGraph decisionGraph = new ThreeDimensionalDecisionGraph(ranking, tokens, new SimpleMatcher(), beginWitness2);
        return decisionGraph;
    }

    enum EditOperationEnum {
        SKIP_GRAPH_RANK,
        SKIP_TOKEN_WITNESS,
        SKIP_TOKEN_GRAPH, // OLD
        MATCH_TOKENS_OR_REPLACE // OLD, but kept for now
    }

    public static class DecisionGraphNode {
        int startPosWitness1 = 0;
        int startPosWitness2 = 0;

        public DecisionGraphNode() {
            this(0,0);
        }

        public DecisionGraphNode(int x, int y) {
            this.startPosWitness1 = x;
            this.startPosWitness2 = y;
        }

        public DecisionGraphNode copy() {
            DecisionGraphNode copy = new DecisionGraphNode();
            copy.startPosWitness1 = this.startPosWitness1;
            copy.startPosWitness2 = this.startPosWitness2;
            return copy;
        }
    }

    public class DecisionGraphNodeCost extends Cost<DecisionGraphNodeCost> {
        // TODO: this is far too simple!
        int alignedTokens; // cost function

        public DecisionGraphNodeCost() {
            this(0);
        }

        public DecisionGraphNodeCost(int alignedTokens) {
            this.alignedTokens = alignedTokens;
        }

        @Override
        protected DecisionGraphNodeCost plus(DecisionGraphNodeCost other) {
            return new DecisionGraphNodeCost(alignedTokens + other.alignedTokens);
        }

        @Override
        public int compareTo(DecisionGraphNodeCost o) {
            return o.alignedTokens - alignedTokens;
        }
    }

    public static class ExtendedGraphNode extends DecisionGraphNode {

        private final int vertexRank;
        private final VariantGraph.Vertex vertex;
        public final Map<ExtendedGraphNode, ExtendedGraphEdge> outgoingEdges;
        protected int cost; // to be assigned by the cost function in the aligner
        protected int heuristicCost; // to be assigned by the heuristic cost function in the aligner

        public ExtendedGraphNode(int vertexRank, VariantGraph.Vertex vertex, int startPosWitness2) {
            this.vertexRank = vertexRank;
            this.vertex = vertex;
            this.startPosWitness2 = startPosWitness2; // index in token array
            this.outgoingEdges = new HashMap<>();
        }

        public int getVertexRank() {
            return vertexRank;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {return true;}
            if (obj == null) {return false;}
            if (!(obj instanceof  ExtendedGraphNode)) {
                return false;
            }
            ExtendedGraphNode other = (ExtendedGraphNode) obj;
            return other.vertex.equals(this.vertex) && other.startPosWitness2 == this.startPosWitness2;
        }

        @Override
        public int hashCode() {
            return vertex.hashCode() + 97 * startPosWitness2;
        }

        public String represent() {
            return String.format("(%d, %d) (%d, %d)", this.getVertexRank(), this.startPosWitness2, this.cost, this.heuristicCost);
        }
    }
    public class ThreeDimensionalDecisionGraph extends AstarAlgorithm<ExtendedGraphNode, DecisionGraphNodeCost> {

        private final VariantGraphRanking ranking;
        private final Token[] witnessTokens;
        private final Matcher matcher;
        private final int startRangeWitness2;
        private final ExtendedGraphNode root;
        private final Map<ExtendedGraphNodeTuple, ExtendedGraphEdge> edges;
        private final Map<ExtendedGraphEdge, ExtendedGraphNode> targets;

        public ThreeDimensionalDecisionGraph(VariantGraphRanking ranking, Token[] witnessTokens, Matcher matcher, int startRangeWitness2) {
            this.ranking = ranking;
            this.witnessTokens = witnessTokens;
            this.matcher = matcher;
            this.startRangeWitness2 = startRangeWitness2;
            //TODO: get by the root node in a better way...
            //TODO: we don need the root node here but the first actual node
            //TODO: thus vertex rank +1
            this.root = new ExtendedGraphNode(0, ranking.getByRank().get(1).iterator().next(), 0);
            edges = new HashMap<>();
            targets = new HashMap<>();
        }

        @Override
        protected boolean isGoal(ExtendedGraphNode node) {
            return isHorizontalEnd(node) && isVerticalEnd(node);
        }

        private boolean isHorizontalEnd(ExtendedGraphNode node) {
            // check whether we have reached the end node of the variant graph
            // -2 because ignore start / end vertices of variant graph
            return node.getVertexRank() == ranking.size()-2;
        }

        private boolean isVerticalEnd(ExtendedGraphNode node) {
            return node.startPosWitness2 == witnessTokens.length;
        }

        @Override
        protected Iterable<ExtendedGraphNode> neighborNodes(ExtendedGraphNode current) {
            //TODO: volgens mij moet het in sommige gevallen rank plus een zijn
            //TODO: namelijk als er op het huidige rank geen gevallen meer zijn
            //TODO: misschien is dit op te lossen
            //TODO: door de huidige rank te doen
            //TODO: en de volgende rank
            //TODO: plus skip huidige rank natuurlijk.. als er niets matched

            // generate decision graph nodes for siblings of this nodes (and maybe child nodes)
            // also a node is needed to skip all the vertices of a certain rank
            // also a node to skip the token of the witness
            List<ExtendedGraphNode> neighbors = new ArrayList<>();

//            The following code is only necessary when there are two or more witnesses
            // TODO: in the siblings we need to skip the current vertex
//            // siblings
//            Set<VariantGraph.Vertex> siblings = ranking.getByRank().get(current.getVertexRank());
//            for (VariantGraph.Vertex vertex : siblings) {
//                ExtendedGraphNode node = new ExtendedGraphNode(current.getVertexRank(), vertex, current.startPosWitness2);
//                neighbors.add(node);
//                ExtendedGraphEdge edge = new ExtendedGraphEdge(EditOperationEnum.MATCH_TOKENS_OR_REPLACE, null);
//                edges.put(current, edge);
//            }

            // skip next graph LCP interval
            VariantGraph.Vertex currentVertex = current.vertex;
            Block graph_interval = vertexToLCP.get(currentVertex);
            //NOTE: not every vertex has to be associated with a LCP interval... non repeated unique blocks of text
            //are not part of an interval, but do have vertices!
            int nextVertexRank;
            if (graph_interval!=null) {
                nextVertexRank = current.getVertexRank() + graph_interval.length;
            } else {
                nextVertexRank = current.getVertexRank() + 1;
                //TODO: this is a hack! We really want to do deal with this cases in a natural manner!
                graph_interval = new Block(tokenIndex, 0, 0);
            }
            if (!isHorizontalEnd(current)) {
                //TODO: dit zouden in theorie meerdere vertices kunenn zijn..
                //TODO: er moet in de constructor van de node moet of de rank of de nextVertex worden meegegeven..
                //TODO: voor de derde dimensie is een nextVertex noodzakelijk
                //TODO: we zouden de graaf af moeten lopen..
                //NOTE: +1 is because of skipping root vertex of variant graph
                //TODO: remove duplication with the root node of decision graph
                VariantGraph.Vertex nextVertex = ranking.getByRank().get(nextVertexRank+1).iterator().next();
                //NOTE: Defensive programming!
                if (currentVertex.equals(nextVertex)) {
                    throw new RuntimeException("current, next Vertex: "+ currentVertex.toString()+" "+nextVertex);
                }
                ExtendedGraphNode node = new ExtendedGraphNode(nextVertexRank, nextVertex, current.startPosWitness2);
                neighbors.add(node);
                addEdge(current, node, EditOperationEnum.SKIP_TOKEN_GRAPH, graph_interval);
            }

            // check whether we are at the end of the variant graph
            // we might not be at the end of the tokens yet
            if (!isHorizontalEnd(current)&&!isVerticalEnd(current)) {
                // children
                //TODO; remove duplication here!
                //NOTE: +1 is because of skipping root vertex of variant graph
                Set<VariantGraph.Vertex> children = ranking.getByRank().get(nextVertexRank+1);
                for (VariantGraph.Vertex vertex : children) {
                    ExtendedGraphNode node = new ExtendedGraphNode(nextVertexRank, vertex, current.startPosWitness2+graph_interval.length);
                    neighbors.add(node);
                    addEdge(current, node, EditOperationEnum.MATCH_TOKENS_OR_REPLACE, graph_interval);
                }
            }

            // skip next witness LCP interval
            if (!isVerticalEnd(current)) {
                // calc position start position witness + position in witness
                int token_position = startRangeWitness2 + current.startPosWitness2;
                Block witness_interval = tokenIndex.getLCP_intervalFor(token_position);
                if (witness_interval==null) {
                    //TODO: this is a hack! We really want to do deal with this cases in a natural manner!
                    witness_interval = new Block(tokenIndex, 0, 0);
                }
                ExtendedGraphNode node = new ExtendedGraphNode(current.getVertexRank(), currentVertex, current.startPosWitness2+witness_interval.length);
                neighbors.add(node);
                addEdge(current, node, EditOperationEnum.SKIP_TOKEN_WITNESS, witness_interval);
            }
            return neighbors;
        }

        private void addEdge(ExtendedGraphNode source, ExtendedGraphNode target, EditOperationEnum operation, Block interval) {
            // Actually there are two intervals!
            if (interval==null) {
                throw new RuntimeException("Interval is null!");
            }
            ExtendedGraphEdge edge = new ExtendedGraphEdge(operation, interval);
            // old style
            edges.put(new ExtendedGraphNodeTuple(source, operation), edge);
            targets.put(edge, target);
            // new style
            source.outgoingEdges.put(target, edge);
        }

        //NOTE: The heuristic cost estimate for the root node of the decision graph is wrong
        //NOTE: this does not yet have consequences because the root node is always selected, after which the heuristic is overwritten
        @Override
        protected DecisionGraphNodeCost heuristicCostEstimate(ExtendedGraphNode node) {
            // traverse the vertices from the current rank to the last
            // one should exclude the current vertex
            int potentialMatchesGraph = 0;
            int vertexRank = node.getVertexRank();
            int endRank = ranking.size();
            // +1 to skip start vertex of variant graph
            // without end rank to skip end vertex of variant graph
            for (int i = vertexRank+1; i < endRank; i++) {
                for (VariantGraph.Vertex vertex : ranking.getByRank().get(i)) {
                    if (vertex != node.vertex && vertexToLCP.get(vertex) != null) {
                        potentialMatchesGraph++;
                    }
                }
            }


//            for (int i = node.startPosWitness1; i < vertices.length; i++) {
//                VariantGraph.Vertex v = vertices[i];
//                if (vertexToLCP.get(v)!=null) {
//                    potentialMatchesGraph++;
//                }
//            }

            int potentialMatchesWitness = 0;
            for (int i = startRangeWitness2 + node.startPosWitness2; i < startRangeWitness2+witnessTokens.length; i++) {
                if (tokenIndex.hasLCP_intervalFor(i)) {
                    potentialMatchesWitness++;
                }
            }
            int potentialMatches = Math.min(potentialMatchesGraph, potentialMatchesWitness);

            // put heuristic cost on the node
            node.heuristicCost = potentialMatches;

            return new DecisionGraphNodeCost(potentialMatches);
        }





        //NOTE: this scorer assigns positive costs
        @Override
        protected DecisionGraphNodeCost distBetween(ExtendedGraphNode current, ExtendedGraphNode neighbor) {
            ExtendedGraphEdge edge = this.edgeBetween(current, EditOperationEnum.MATCH_TOKENS_OR_REPLACE);
            if (edge!=null&&this.getTarget(edge).equals(neighbor)) {
                Block graphInterval = edge.block;
                Block witnessInterval = tokenIndex.getLCP_intervalFor(startRangeWitness2+current.startPosWitness2);
                if (graphInterval==witnessInterval) {
                    edge.match = true;
                    // set cost on neighbor if it is higher
                    if (neighbor.cost < graphInterval.length) {
                        neighbor.cost = graphInterval.length;
                    }
                    return new DecisionGraphNodeCost(graphInterval.length);
                }
            }
            return new DecisionGraphNodeCost(0);
        }

        public Token token(DecisionGraphNode node) {
            return witnessTokens[node.startPosWitness2];
        }

        public VariantGraph.Vertex vertex(DecisionGraphNode node) {
            return ((ExtendedGraphNode)node).vertex;
        }

        public ExtendedGraphNode getRoot() {
            return root;
        }

        public ExtendedGraphEdge edgeBetween(ExtendedGraphNode source, EditOperationEnum operation) {
            ExtendedGraphEdge edge = edges.get(new ExtendedGraphNodeTuple(source, operation));
//            if (edge == null) {
//                throw new RuntimeException("Edge not present!");
//            }
            return edge;
        }

        public ExtendedGraphNode getTarget(ExtendedGraphEdge edge) {
            ExtendedGraphNode target = targets.get(edge);
            if (target == null) {
                throw new RuntimeException("Target not set!");
            }
            return target;
        }

        public List<ExtendedGraphEdge> getOptimalPath() {
            List<ExtendedGraphNode> nodes = this.aStar(getRoot(), new DecisionGraphNodeCost());
            if (nodes.isEmpty()) {
                throw new RuntimeException("Nodes are unexpected empty!");
            }
            // transform the nodes into edges
            Deque<ExtendedGraphNode> nodesTodo = new ArrayDeque<>();
            nodesTodo.addAll(nodes);
            List<ExtendedGraphEdge> edges = new ArrayList<>();
            while(!nodesTodo.isEmpty()) {
                ExtendedGraphNode node = nodesTodo.pop();
                if (isGoal(node)) {
                    break;
                }
                ExtendedGraphNode next = nodesTodo.peek();
                ExtendedGraphEdge edge = this.getEdgeBetween(node, next);
                edges.add(edge);
            }
            return edges;

        }

        private ExtendedGraphEdge getEdgeBetween(ExtendedGraphNode node, ExtendedGraphNode next) {
            ExtendedGraphEdge edge = node.outgoingEdges.get(next);
            return edge;
        }
    }

    public class ExtendedGraphNodeTuple {
        private ExtendedGraphNode source;
        private EditOperationEnum operation;
        //private ExtendedGraphNode target;

        public ExtendedGraphNodeTuple(ExtendedGraphNode source, EditOperationEnum operation) {
            this.source = source;
            this.operation = operation;
        }

        //TODO: with target hashcode added performance would be improved
        @Override
        public int hashCode() {
            return source.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof ExtendedGraphNodeTuple)) {
                return false;
            }
            ExtendedGraphNodeTuple other = (ExtendedGraphNodeTuple) obj;
            return other.source.equals(source) && other.operation == operation;
        }
    }

    public class ExtendedGraphEdge {
        protected EditOperationEnum operation;
        protected Block block;
        private boolean match;

        public ExtendedGraphEdge(EditOperationEnum operation, Block block) {
            this.operation = operation;
            this.block = block;
        }

        public String represent(Dekker21Aligner aligner) {
            String result = "";
            if (operation == EditOperationEnum.SKIP_TOKEN_GRAPH) {
                result += "remove";
            } else if (operation == EditOperationEnum.MATCH_TOKENS_OR_REPLACE) {
                result += "match/replace";
            } else if (operation == EditOperationEnum.SKIP_TOKEN_WITNESS) {
                result += "add";
            }
            result += " (";
            result += block.getNormalizedForm();
            result += ")";
            return result;
        }

        public boolean isMatch() {
            return match;
        }
    }

    private int getLowestTokenPosition(Block lcpInterval) {
        // search lowest token position in lcp interval
        // that position will already be in the variant graph
        int suffixStart = lcpInterval.start;
        int suffixEnd = lcpInterval.end;
        int lowestPosition = 0;
        for (int i = suffixStart; i <= suffixEnd; i++) {
            int tokenPosition = tokenIndex.suffix_array[i];
            if (tokenPosition < lowestPosition)
                lowestPosition = tokenPosition;
        }
        return lowestPosition;
    }





    public static <T> List<T> copyIterable(Iterable<T> iterable) {
        return copyIterator(iterable.iterator());
    }

    public static <T> List<T> copyIterator(Iterator<T> iter) {
        List<T> copy = new ArrayList<T>();
        while (iter.hasNext())
            copy.add(iter.next());
        return copy;
    }



    class Matcher {
        Boolean match(VariantGraph.Vertex a, Token b) {
            return false;
        }

    }

    class SimpleMatcher extends Matcher {
        @Override
        Boolean match(VariantGraph.Vertex a, Token b) {
            SimpleToken sa = (SimpleToken) a.tokens().iterator().next();
            SimpleToken sb = (SimpleToken) b;
            return sa.getNormalized().equals(sb.getNormalized());
        }
    }



//    // 1) We need to know the neighbors in the decision graph
//    // There are three possibilities in a 2D space
//    // 2) We need to calculate the heuristic for each
//    // 3) We need to score each
//    class TwoDimensionalDecisionGraph extends AstarAlgorithm<DecisionGraphNode, DecisionGraphNodeCost> {
//
//        private final VariantGraph.Vertex[] vertices;
//        private final Token[] tokens;
//        private int startRangeWitness2;
//        private Matcher matcher;
//
//        public TwoDimensionalDecisionGraph(VariantGraph.Vertex[] vertices, Token[] tokens, int startRangeWitness2) {
//            this.vertices = vertices;
//            this.tokens = tokens;
//            this.startRangeWitness2 = startRangeWitness2;
//            this.matcher = new SimpleMatcher();
//        }
//
//        @Override
//        protected boolean isGoal(DecisionGraphNode node) {
//            return isHorizontalEnd(node) && isVerticalEnd(node);
//        }
//
//        private boolean isHorizontalEnd(DecisionGraphNode node) {
//            return node.startPosWitness1 == vertices.length;
//        }
//
//        private boolean isVerticalEnd(DecisionGraphNode node) {
//            return node.startPosWitness2 == tokens.length;
//        }
//
//        @Override
//        protected Iterable<DecisionGraphNode> neighborNodes(DecisionGraphNode current) {
//            // In a 2D approach there are 3 possibilities
//            List<DecisionGraphNode> children = new ArrayList<>();
//            boolean xEnd = isHorizontalEnd(current);
//            boolean yEnd = isVerticalEnd(current);
//            if (!xEnd) {
//                DecisionGraphNode child1 = current.copy();
//                child1.startPosWitness1++;
//                child1.editOperation = EditOperationEnum.SKIP_TOKEN_GRAPH;
//                children.add(child1);
//            }
//            if (!yEnd) {
//                DecisionGraphNode child2 = current.copy();
//                child2.startPosWitness2++;
//                child2.editOperation = EditOperationEnum.SKIP_TOKEN_WITNESS;
//                children.add(child2);
//            }
//            if (!xEnd && !yEnd) {
//                DecisionGraphNode child3 = current.copy();
//                child3.startPosWitness1++;
//                child3.startPosWitness2++;
//                child3.editOperation = EditOperationEnum.MATCH_TOKENS_OR_REPLACE;
//                children.add(child3);
//            }
//            return children;
//        }
//
//        @Override
//        protected DecisionGraphNodeCost heuristicCostEstimate(DecisionGraphNode node) {
//            int potentialMatchesGraph = 0;
//            for (int i = node.startPosWitness1; i < vertices.length; i++) {
//                VariantGraph.Vertex v = vertices[i];
//                if (vertexToLCP.get(v)!=null) {
//                    potentialMatchesGraph++;
//                }
//            }
//
//            int potentialMatchesWitness = 0;
//            for (int i = startRangeWitness2 + node.startPosWitness2; i < startRangeWitness2+tokens.length; i++) {
//                if (lcp_interval_array[i] != null) {
//                    potentialMatchesWitness++;
//                }
//            }
//            int potentialMatches = Math.min(potentialMatchesGraph, potentialMatchesWitness);
//            return new DecisionGraphNodeCost(potentialMatches);
//        }
//
//        //NOTE: this scorer assigns positive costs
//        @Override
//        protected DecisionGraphNodeCost distBetween(DecisionGraphNode current, DecisionGraphNode neighbor) {
//            if (neighbor.editOperation == EditOperationEnum.MATCH_TOKENS_OR_REPLACE) {
//                VariantGraph.Vertex v = vertex(neighbor);
//                Token t = token(neighbor);
//                Boolean match = matcher.match(v, t);
//                if (match) {
//                    // Log("match: "+(neighbor.startPosWitness1-1)+", "+(neighbor.startPosWitness2-1));
//                    neighbor.match = true;
//                    return new DecisionGraphNodeCost(1);
//                }
//            }
//            return new DecisionGraphNodeCost(0);
//        }
//
//        public VariantGraph.Vertex vertex(DecisionGraphNode node) {
//            return vertices[node.startPosWitness1-1];
//        }
//
//        public Token token(DecisionGraphNode node) {
//            return tokens[node.startPosWitness2-1];
//        }
//   }

}
