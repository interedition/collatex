package eu.interedition.collatex.dekker.new_align;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.island.Island;
import eu.interedition.collatex.dekker.token_index.TokenIndex;
import eu.interedition.collatex.dekker.token_index.TokenIndexToMatches;
import eu.interedition.collatex.util.VariantGraphRanking;

import java.util.*;

/**
 * Created by ronalddekker on 21/10/15.
 */
public class AlignmentPhase {

    //TODO: the next field is public because of test... must be a better way to do this
    public List<Island> phraseMatchesGraphOrder;
    public List<Island> phraseMatchesWitnessOrder;

    public void doAlign(TokenIndex tokenIndex, VariantGraph graph, Iterable<Token> tokens, VariantGraph.Vertex[] vertexArray) {
        // we need to get the potential matches from the token index
        Set<Island> allPossibleIslands = TokenIndexToMatches.createMatches(tokenIndex, vertexArray, graph, tokens);

        // aansluiten op oude code
        //converteer naar gewoon grote lijst van Matches
        //NOTE: A coordinate is an extended match
        //ISland is a weird form of a phrase match

        List<Island> phraseMatches = new ArrayList<>();
        phraseMatches.addAll(allPossibleIslands);
        System.out.println("Debug: "+phraseMatches);

        VariantGraph base = graph;

        // rank the graph (needed to sort the phrase matches)
        final VariantGraphRanking ranking = rankTheGraph(phraseMatches, base);
        // sort the blocks based on graph order (second witness order)
        sortPhraseMatchesBasedOnGraphOrder(phraseMatches, ranking);
        // sort the blocks based on witness order
        sortPhraseMatchesOnWitnessOrder(phraseMatches, ranking);


        // build a table in which the decisions are made based on traversing the two blocks arrays
        // score diagnally

        // for each cell we need to keep track of a lot of information
        // to base the scoring on
    }

    private void sortPhraseMatchesBasedOnGraphOrder(List<Island> phraseMatches, VariantGraphRanking ranking) {
        /*
         * We order the phrase matches in the topological order
        * of the graph (called rank). When the rank is equal
        * for two phrase matches, the witness order is used
        * to differentiate.
        */

        Comparator<Island> comp = (pm1, pm2) -> {
            // sort on 1) rank in the graph (lower rank first)
            int rank1 = ranking.apply(pm1.getMatch(0).vertex);
            int rank2 = ranking.apply(pm2.getMatch(0).vertex);
            int difference = rank1 - rank2;
            if (difference != 0) {
                return difference;
            }
            // sort on 2) length of the phrase match (larger first)
            int length1 = pm1.size();
            int length2 = pm2.size();
            int difference2 = length2 - length1;
            if (difference2 != 0) {
                return difference2;
            }
            // sort on 3) position in the witness (lower position first)
            int index1 = pm1.getLeftEnd().row;
            int index2 = pm2.getLeftEnd().row;
            return index1 - index2;
        };

        phraseMatchesGraphOrder = new ArrayList<>(phraseMatches);
        Collections.sort(phraseMatchesGraphOrder, comp);

        System.out.println(phraseMatchesGraphOrder);
    }

    private void sortPhraseMatchesOnWitnessOrder(List<Island> phraseMatches, VariantGraphRanking ranking) {
        Comparator<Island> comp2 = (pm1, pm2) -> {
            // sort on 1) position in the witness (lower position first)
            int index1 = pm1.getLeftEnd().row;
            int index2 = pm2.getLeftEnd().row;
            int difference = index1 - index2;
            if (difference != 0) {
                return difference;
            }
            // sort on 2) length of the phrase match (larger first)
            int length1 = pm1.size();
            int length2 = pm2.size();
            int difference2 = length2 - length1;
            if (difference2 != 0) {
                return difference2;
            }
            // sort on 3) rank in the graph (lowest rank first)
            int rank1 = ranking.apply(pm1.getMatch(0).vertex);
            int rank2 = ranking.apply(pm2.getMatch(0).vertex);
            int difference3 = rank1 - rank2;
            return difference3;
        };

        phraseMatchesWitnessOrder = new ArrayList<>(phraseMatches);
        Collections.sort(phraseMatchesWitnessOrder, comp2);

        System.out.println(phraseMatchesWitnessOrder);
    }

    /*
    * There is a case in the Beckett project where the transposition detection screws up if
    * non matched vertices are counted in the rank calculation
    * I am not sure whether I have a unit test to assert this case
     */
    private VariantGraphRanking rankTheGraph(List<Island> phraseMatches, VariantGraph base) {
        // rank the variant graph
        Set<VariantGraph.Vertex> matchedVertices = new HashSet<>();
        for (Island phraseMatch : phraseMatches) {
            matchedVertices.add(phraseMatch.getMatch(0).vertex);
        }
        final VariantGraphRanking ranking = VariantGraphRanking.ofOnlyCertainVertices(base, matchedVertices);
        return ranking;
    }

}
