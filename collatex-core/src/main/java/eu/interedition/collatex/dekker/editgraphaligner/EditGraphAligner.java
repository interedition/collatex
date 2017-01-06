package eu.interedition.collatex.dekker.editgraphaligner;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.island.Coordinate;
import eu.interedition.collatex.dekker.island.Island;
import eu.interedition.collatex.dekker.token_index.TokenIndex;
import eu.interedition.collatex.dekker.token_index.TokenIndexToMatches;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.StreamSupport;

/**
 * Created by Ronald Haentjens Dekker on 06/01/17.
 *
 * This class tries to combine idea's of the Java version of cox with the Python version of cx
 * It uses the TokenIndex (suffix array, lcp array, lcp intervals) that was first pioneered in the python version
 * But then it uses the TokenIndexToMatches 3d matches (cube) between the variant graph and the next witness,
 * using the token index.
 *
 */
public class EditGraphAligner extends CollationAlgorithm.Base {
    public TokenIndex tokenIndex;
    // tokens are mapped to vertices by their position in the token array
    protected VariantGraph.Vertex[] vertex_array;
    private final Comparator<Token> comparator;

    public EditGraphAligner() {
        this(new EqualityTokenComparator());
    }

    public EditGraphAligner(Comparator<Token> comparator) {
        this.comparator = comparator;
    }



    @Override
    public void collate(VariantGraph graph, List<? extends Iterable<Token>> witnesses) {
        // phase 1: matching phase
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Building token index from the tokens of all witnesses");
        }

        this.tokenIndex = new TokenIndex(comparator, witnesses);
        tokenIndex.prepare();

        // phase 2: alignment phase
        this.vertex_array = new VariantGraph.Vertex[tokenIndex.token_array.length];
        boolean firstWitness = true;

        for (Iterable<Token> tokens : witnesses) {
            final Witness witness = StreamSupport.stream(tokens.spliterator(), false)
                .findFirst()
                .map(Token::getWitness)
                .orElseThrow(() -> new IllegalArgumentException("Empty witness"));

            // first witness has a fast path
            if (firstWitness) {
                super.merge(graph, tokens, Collections.emptyMap());
                updateTokenToVertexArray(tokens, witness);
                firstWitness = false;
                continue;
            }

            // align second, third, fourth witness etc.
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "{0} + {1}: {2} vs. {3}", new Object[]{graph, witness, graph.vertices(), tokens});
            }

            // Phase 2a: Gather matches from the token index
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0} + {1}: Gather matches between variant graph and witness from token index", new Object[]{graph, witness});
            }

            Set<Island> allPossibleIslands = TokenIndexToMatches.createMatches(tokenIndex, vertex_array, graph, tokens);
            // print all the possible islands as separate matches...
                for (Island i : allPossibleIslands) {
                    for (Coordinate c : i) {
                        System.out.println(c);
                    }
                }
                // apparently there are doubles in the coordinates

                // now we can create the space for the edit graph.. using arrays and stuff

            // the vertical size is the number of vertices in the graph minus the start and end vertices
            // NOTE: THe token index to matches already does a graph ranking!
            VariantGraphRanking variantGraphRanking = VariantGraphRanking.of(graph);
                // oh wait there are more methods on the java variant graph ranking!
            List<Integer> verticesAsRankList = new ArrayList<>();
            for (VariantGraph.Vertex vertex : graph.vertices()) {
                int rank = variantGraphRanking.getByVertex().get(vertex);
                verticesAsRankList.add(rank);
            }
            // we leave the start vertex in (that is an extra position that is needed in the edit graph)
            // we remove the end vertex though
            verticesAsRankList.remove(verticesAsRankList.size()-1);

            System.out.println("horizontal (graph): "+ verticesAsRankList);

            // now the vertical stuff
            List<Token> witnessTokens = new ArrayList<>();
            for (Token t : tokens) {
                witnessTokens.add(t);
            }
            List<Integer> tokensAsIndexList = new ArrayList<>();
            tokensAsIndexList.add(0);
            int counter = 1;
            for (Token t : tokens) {
                tokensAsIndexList.add(counter++);
            }
            System.out.println("vertical (next witness): "+tokensAsIndexList);


            // TODO: remove this break!
            break;
        }

    }

    private void updateTokenToVertexArray(Iterable<Token> tokens, Witness witness) {
        // we need to update the token -> vertex map
        // that information is stored in protected map
        int tokenPosition = tokenIndex.getStartTokenPositionForWitness(witness);
        for (Token token : tokens) {
            VariantGraph.Vertex vertex = witnessTokenVertices.get(token);
            vertex_array[tokenPosition] = vertex;
            tokenPosition++;
        }
    }

    public void collate(VariantGraph against, Iterable<Token> witness) {

    }
}
