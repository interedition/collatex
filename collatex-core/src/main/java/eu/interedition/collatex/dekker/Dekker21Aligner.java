package eu.interedition.collatex.dekker;

import java.util.*;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.astar.AstarAlgorithm;
import eu.interedition.collatex.dekker.astar.Cost;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.suffixarray.SAIS;
import eu.interedition.collatex.suffixarray.SuffixArrays;
import eu.interedition.collatex.suffixarray.SuffixData;
import eu.interedition.collatex.util.VariantGraphRanking;

public class Dekker21Aligner extends CollationAlgorithm.Base {

    private List<Token> token_array;
    private int[] suffix_array;
    protected int[] LCP_array;
    private List<LCP_Interval> lcp_intervals;
    private LCP_Interval[] lcp_interval_array;
    private Map<VariantGraph.Vertex, LCP_Interval> vertexToLCP;
    private Map<Witness, Integer> witnessToStartToken;
    private ThreeDimensionalDecisionGraph astar;

    public Dekker21Aligner(SimpleWitness[] w) {
        // 1. prepare token array
        // 2. derive the suffix array
        // 3. derive LCP array
        // 4. derive LCP intervals
        token_array = new ArrayList<>();
        int counter = 0;
        witnessToStartToken = new HashMap<>();
        for (SimpleWitness witness : w) {
            witnessToStartToken.put(witness, counter);
            for (Token t : witness) {
                token_array.add(t);
                counter++;
            }
            //TODO: add witness separation marker token
        }
        Comparator<Token> comparator = new SimpleTokenNormalizedFormComparator();
        SuffixData suffixData = SuffixArrays.createWithLCP(token_array.toArray(new Token[0]), new SAIS(), comparator);
        suffix_array = suffixData.getSuffixArray();
        LCP_array = suffixData.getLCP();
        vertexToLCP = new HashMap<>();
        lcp_intervals = splitLCP_ArrayIntoIntervals();
        lcp_interval_array = construct_LCP_interval_array();
    }

    protected List<LCP_Interval> splitLCP_ArrayIntoIntervals() {
        List<LCP_Interval> closedIntervals = new ArrayList<>();
        int previousLCP_value = 0;
        Stack<LCP_Interval> openIntervals = new Stack<LCP_Interval>();
        for (int idx = 0; idx < LCP_array.length; idx++) {
            int lcp_value = LCP_array[idx];
            if (lcp_value > previousLCP_value) {
                openIntervals.push(new LCP_Interval(idx - 1, lcp_value));
                previousLCP_value = lcp_value;
            } else if (lcp_value < previousLCP_value) {
                // close open intervals that are larger than current LCP value
                while (!openIntervals.isEmpty() && openIntervals.peek().length > lcp_value) {
                    LCP_Interval a = openIntervals.pop();
                    closedIntervals.add(new LCP_Interval(a.start, idx - 1, a.length));
                }
                // then: open a new interval starting with filtered intervals
                if (lcp_value > 0) {
                    int start = closedIntervals.get(closedIntervals.size() - 1).start;
                    openIntervals.add(new LCP_Interval(start, lcp_value));
                }
                previousLCP_value = lcp_value;
            }
        }
        // add all the open intervals to the result
        for (LCP_Interval interval : openIntervals) {
            closedIntervals.add(new LCP_Interval(interval.start, this.LCP_array.length - 1, interval.length));
        }
        return closedIntervals;
    }

    protected String debug(LCP_Interval interval) {
        return interval.toString() + " -> " + getNormalizedForm(interval);
    }

    protected String getNormalizedForm(LCP_Interval interval) {
        int suffix_start = interval.start;
        int token_pos = this.suffix_array[suffix_start];
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < interval.length; i++) {
            Token t = this.token_array.get(token_pos+i);
            tokens.add(t);
        }
        String normalized = "";
        for (Token t : tokens) {
            SimpleToken st = (SimpleToken) t;
            if (!normalized.isEmpty()) {
                normalized += " ";
            }
            normalized += st.getNormalized();
        }
        return normalized;
    }

    //TODO: remove! Replace with createDecisionGraph
    protected ThreeDimensionalDecisionGraph getDecisionGraph() {
        if (astar == null) {
            throw new IllegalStateException("Collate something first!");
        }
        return astar;
    }


    private LCP_Interval[] construct_LCP_interval_array() {
        LCP_Interval[] lcp_interval_array = new LCP_Interval[token_array.size()];
        for (LCP_Interval interval : lcp_intervals) {
            for (int i = interval.start; i <= interval.end; i++) {
                int tokenIndex = suffix_array[i];
                //Log("Adding interval: " + interval.toString() + " to token number: " + tokenIndex);
                lcp_interval_array[tokenIndex] = interval;
            }
        }
        return lcp_interval_array;
    }

    @Override
    public void collate(VariantGraph against, Iterable<Token> witness) {
        // first witness?
        boolean first_witness = vertexToLCP.isEmpty();
        if (first_witness) {
            super.merge(against, witness, new HashMap<>());
            // need to update vertex to lcp map

            // we need witness token -> vertex
            // that information is stored in protected map
            int tokenPosition = 0;
            for (Token token : witness) {
                VariantGraph.Vertex vertex = witnessTokenVertices.get(token);
                LCP_Interval interval = lcp_interval_array[tokenPosition];
                vertexToLCP.put(vertex, interval);
                tokenPosition++;
            }
            return;
        }
        //NOTE: the following method assigns the decision graph to the field astar
        createDecisionGraph(against, witness);


        // Do the actual alignment
        List<ExtendedGraphNode> nodes = astar.aStar(new ExtendedGraphNode(0, against.getStart(), 0), new DecisionGraphNodeCost());
        // from the list of nodes we need to extract the matches
        // and construct a map (token -> vertex) containing the alignment.
        Map<Token, VariantGraph.Vertex> alignments = new HashMap<>();
        for (DecisionGraphNode node : nodes) {
            if (node.isMatch()) {
                alignments.put(astar.token(node), astar.vertex(node));
            }
        }
        merge(against, witness, alignments);
    }

    public ThreeDimensionalDecisionGraph createDecisionGraph(VariantGraph against, Iterable<Token> witness) {
        int beginWitness2 = witnessToStartToken.get(witness.iterator().next().getWitness());

        // prepare vertices
        List<VariantGraph.Vertex> vert = copyIterable(against.vertices());
        // remove start / end vertices
        vert.remove(0);
        vert.remove(vert.size() - 1);
        VariantGraph.Vertex[] vertices = vert.toArray(new VariantGraph.Vertex[vert.size()]);

        // prepare tokens
        List<Token> tok = copyIterable(witness);
        Token[] tokens = tok.toArray(new Token[tok.size()]);

        // Align using astar algorithm in 2D
        // first dimension: vertices in topological order
        // second dimension: witness tokens
        // TODO: This should be done in a 3D space. 2D causes artifacts, since there are multiple topological sorts of the graph.
        // TODO: first dimension: vertex ranks
        // TODO: second dimension: all vertices of a certain rank
        // TODO: third dimension: witness tokens

        VariantGraphRanking ranking = VariantGraphRanking.of(against);
        astar = new ThreeDimensionalDecisionGraph(ranking, tokens, new SimpleMatcher(), beginWitness2);
        return astar;
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
        //TODO: move to edge!
        private boolean match;

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

        public boolean isMatch() {
            return match;
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

        private int vertexRank;
        private final VariantGraph.Vertex vertex;

        public ExtendedGraphNode(int vertexRank, VariantGraph.Vertex vertex, int startPosWitness2) {
            this.vertexRank = vertexRank;
            this.vertex = vertex;
            this.startPosWitness2 = startPosWitness2; // index in token array
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
            LCP_Interval graph_interval = vertexToLCP.get(current.vertex);
            int nextVertexRank = current.getVertexRank() + graph_interval.length;
            if (!isHorizontalEnd(current)) {
                //TODO: dit zouden in theorie meerdere vertices kunenn zijn..
                //TODO: er moet in de constructor van de node moet of de rank of de vertex worden meegegeven..
                //TODO: voor de derde dimensie is een vertex noodzakelijk
                //TODO: we zouden de graaf af moeten lopen..
                VariantGraph.Vertex vertex = ranking.getByRank().get(nextVertexRank).iterator().next();
                ExtendedGraphNode node = new ExtendedGraphNode(nextVertexRank, vertex, current.startPosWitness2);
                neighbors.add(node);
                addEdge(current, node, EditOperationEnum.SKIP_TOKEN_GRAPH, graph_interval);
            }

            // check whether we are at the end of the variant graph
            // we might not be at the end of the tokens yet
            if (!isHorizontalEnd(current)&&!isVerticalEnd(current)) {
                // children
                Set<VariantGraph.Vertex> children = ranking.getByRank().get(nextVertexRank);
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
                LCP_Interval witness_interval = lcp_interval_array[token_position];
                ExtendedGraphNode node = new ExtendedGraphNode(current.getVertexRank(), current.vertex, current.startPosWitness2+witness_interval.length);
                neighbors.add(node);
                addEdge(current, node, EditOperationEnum.SKIP_TOKEN_WITNESS, witness_interval);
            }
            return neighbors;
        }

        private void addEdge(ExtendedGraphNode source, ExtendedGraphNode target, EditOperationEnum operation, LCP_Interval interval) {
            // Actually there are two intervals!
            if (interval==null) {
                throw new RuntimeException("Interval is null!");
            }
            ExtendedGraphEdge edge = new ExtendedGraphEdge(operation, interval);
            edges.put(new ExtendedGraphNodeTuple(source, operation), edge);
            targets.put(edge, target);
        }

        @Override
        protected DecisionGraphNodeCost heuristicCostEstimate(ExtendedGraphNode node) {
            // traverse the vertices from the current rank to the last
            // one should exclude the current vertex
            int potentialMatchesGraph = 0;
            int vertexRank = node.getVertexRank();
            int endRank = ranking.size();

            for (int i = vertexRank; i < endRank; i++) {
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
                if (lcp_interval_array[i] != null) {
                    potentialMatchesWitness++;
                }
            }
            int potentialMatches = Math.min(potentialMatchesGraph, potentialMatchesWitness);
            return new DecisionGraphNodeCost(potentialMatches);
        }

        @Override
        protected DecisionGraphNodeCost distBetween(ExtendedGraphNode current, ExtendedGraphNode neighbor) {
            //TODO: FIX
            return new DecisionGraphNodeCost(0);
        }

        public Token token(DecisionGraphNode node) {
            return witnessTokens[node.startPosWitness2-1];
        }

        public VariantGraph.Vertex vertex(DecisionGraphNode node) {
            return ((ExtendedGraphNode)node).vertex;
        }

        public ExtendedGraphNode getRoot() {
            return root;
        }

        public ExtendedGraphEdge edgeBetween(ExtendedGraphNode source, EditOperationEnum operation) {
            ExtendedGraphEdge edge = edges.get(new ExtendedGraphNodeTuple(source, operation));
            if (edge == null) {
                throw new RuntimeException("Edge not present!");
            }
            return edge;
        }

        public ExtendedGraphNode getTarget(ExtendedGraphEdge edge) {
            ExtendedGraphNode target = targets.get(edge);
            if (target == null) {
                throw new RuntimeException("Target not set!");
            }
            return target;
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
        protected LCP_Interval lcp_interval;

        public ExtendedGraphEdge(EditOperationEnum operation, LCP_Interval lcp_interval) {
            this.operation = operation;
            this.lcp_interval = lcp_interval;
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

    public static <T> List<T> copyIterable(Iterable<T> iterable) {
        return copyIterator(iterable.iterator());
    }

    public static <T> List<T> copyIterator(Iterator<T> iter) {
        List<T> copy = new ArrayList<T>();
        while (iter.hasNext())
            copy.add(iter.next());
        return copy;
    }
}
