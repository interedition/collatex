package eu.interedition.collatex.dekker.matrix;

import java.util.Collection;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class MatchTableModifier {

  /*
   * For all the possible islands of a certain size this method checks whether
   * they conflict with one of the previously committed islands. If so, the
   * possible island is removed from the multimap. Or in case of overlap, split
   * into a smaller island and then put in back into the map Note that this
   * method changes the possible islands multimap.
   */
  //TODO: the original Island object is modified here
  //TODO: That should not happen, if we want to build a decision tree.
  public static void removeOrSplitImpossibleIslands(MatchTableSelection selection, Integer islandSize, Multimap<Integer, Island> islandMultimap) {
    Collection<Island> islandsToCheck = Lists.newArrayList(islandMultimap.get(islandSize));
    for (Island island : islandsToCheck) {
      if (!selection.isIslandPossibleCandidate(island)) {
        islandMultimap.remove(islandSize, island);
        removeConflictingEndCoordinates(selection, island);
        if (island.size() > 0) {
          islandMultimap.put(island.size(), island);
        }
      }
    }
  }

  private static void removeConflictingEndCoordinates(MatchTableSelection selection, Island island) {
    boolean goOn = true;
    while (goOn) {
      Coordinate leftEnd = island.getLeftEnd();
      if (selection.doesCoordinateOverlapWithCommittedCoordinate(leftEnd)) {
        island.removeCoordinate(leftEnd);
        if (island.size() == 0) {
          return;
        }
      } else {
        goOn = false;
      }
    }
    goOn = true;
    while (goOn) {
      Coordinate rightEnd = island.getRightEnd();
      if (selection.doesCoordinateOverlapWithCommittedCoordinate(rightEnd)) {
        island.removeCoordinate(rightEnd);
        if (island.size() == 0) {
          return;
        }
      } else {
        goOn = false;
      }
    }
  }
}
