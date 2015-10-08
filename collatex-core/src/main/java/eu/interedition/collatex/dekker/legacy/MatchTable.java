package eu.interedition.collatex.dekker.legacy;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.matrix.Island;

import java.util.List;
import java.util.Set;

/**
 * Created by ronald on 4/26/15.
 */
public interface MatchTable {
    VariantGraph.Vertex vertexAt(int rowIndex, int columnIndex);

    Token tokenAt(int rowIndex, int columnIndex);

    // Warning: this method reiterates the witness!
    // This method is only meant for the user interface and serialization classes!
    // Use the tokenAt method in all other cases.
    List<Token> rowList();

    List<Integer> columnList();

    // Since the coordinates in allMatches are ordered from upper left to lower right,
    // we don't need to check the lower right neighbor.
    Set<Island> getIslands();
}
