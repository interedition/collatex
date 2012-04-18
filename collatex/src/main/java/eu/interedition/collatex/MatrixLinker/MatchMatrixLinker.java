package eu.interedition.collatex.matrixlinker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.TokenLinker;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.matching.Matches;
import eu.interedition.collatex.matrixlinker.MatchMatrix.Island;

public class MatchMatrixLinker implements TokenLinker {

  @Override
  public Map<Token, VariantGraphVertex> link(VariantGraph base, Iterable<Token> witness, Comparator<Token> comparator) {
    // TODO Auto-generated method stub

    MatchMatrix buildMatrix = buildMatrix(base, witness, comparator);
    ArchipelagoWithVersions archipelago = new ArchipelagoWithVersions();
    for (MatchMatrix.Island isl : buildMatrix.getIslands()) {
      archipelago.add(isl);
    }
    Archipelago createFirstVersion = archipelago.createFirstVersion();
    ArrayList<Island> iterator = createFirstVersion.iterator();
    // bepaal kleur cel ahv firstversion
    //    int mat[] = new int[rowNum()];
    //
    //    for (Island island : iterator) {
    //      island.ge
    //    }
    //    ArrayList<Coordinates> gaps = createFirstVersion.findGaps();
    //    for (Coordinates coordinates : gaps) {
    //      coordinates.
    //    }
    Map<Token, VariantGraphVertex> map = Maps.newHashMap();
    return map;
  }

  public static MatchMatrix buildMatrix(VariantGraph base, Iterable<Token> witness, Comparator<Token> comparator) {
    base.rank();
    Matches matches = Matches.between(base.vertices(), witness, comparator);
    MatchMatrix arrayTable = new MatchMatrix(base.vertices(), witness);
    Set<Token> unique = matches.getUnique();
    Set<Token> ambiguous = matches.getAmbiguous();
    int column = 0;
    for (Token t : witness) {
      if (unique.contains(t)) {
        arrayTable.set(matches.getAll().get(t).get(0).getRank() - 1, column, true);
      } else {
        if (ambiguous.contains(t)) {
          for (VariantGraphVertex vgv : matches.getAll().get(t)) {
            arrayTable.set(vgv.getRank() - 1, column, true);
          }
        }
      }
      column++;
    }
    return arrayTable;
  }

}
