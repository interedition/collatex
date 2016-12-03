package eu.interedition.collatex.dekker.new_align;

import eu.interedition.collatex.CollationAlgorithm;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.dekker.island.Island;
import eu.interedition.collatex.dekker.token_index.TokenIndex;
import eu.interedition.collatex.dekker.token_index.TokenIndexToMatches;
import eu.interedition.collatex.matching.EqualityTokenComparator;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

/**
 * Created by Ronald Dekker on 03/12/16.
 */
public class AnotherAttemptAtAnAligner extends CollationAlgorithm.Base {
    private TokenIndex tokenIndex;
    private VariantGraph.Vertex[] token_to_vertex_array;
    DecisionTree decisionTree;

    @Override
    public void collate(VariantGraph against, Iterable<Token> witness) {
        throw new UnsupportedOperationException("Supply all the witnesses at the same time!");
    }

    @Override
    public void collate(VariantGraph graph, List<? extends Iterable<Token>> witnesses) {
        // We first build the token index for all the witnesses
        // This saves the cost of matching tokens over and over again
        // It also gives a heuristic value for the maximum potential matches in a certain alignment
        this.tokenIndex = new TokenIndex(new EqualityTokenComparator(), witnesses);
        tokenIndex.prepare();

        // Second we build the Vertex Array: it maps every token in the token array to a vertex.
        this.token_to_vertex_array = new VariantGraph.Vertex[tokenIndex.token_array.length];

        // Align the first witness
        Iterable<Token> tokens = witnesses.get(0);
        final Witness witness = StreamSupport.stream(tokens.spliterator(), false)
            .findFirst()
            .map(Token::getWitness)
            .orElseThrow(() -> new IllegalArgumentException("Empty witness"));

        // That is easy, just make a vertex for every token..
        super.merge(graph, tokens, Collections.emptyMap());
        updateTokenToVertexArray(tokens, witness);

        // For the second witness we have to get the potential matches from the token index
        tokens = witnesses.get(1);

        // We now build a decision tree of matches. For now we build the complete tree. This could be done lazily.
        DecisionTreeBuilder builder = new DecisionTreeBuilder();
        this.decisionTree = builder.create(tokenIndex, graph, tokens, token_to_vertex_array);





    }

    private void updateTokenToVertexArray(Iterable<Token> tokens, Witness witness) {
        // we need to update the token -> vertex map
        // that information is stored in protected map
        int tokenPosition = tokenIndex.getStartTokenPositionForWitness(witness);
        for (Token token : tokens) {
            VariantGraph.Vertex vertex = witnessTokenVertices.get(token);
            token_to_vertex_array[tokenPosition] = vertex;
            tokenPosition++;
        }
    }
}
