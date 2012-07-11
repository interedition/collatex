package eu.interedition.collatex.dekker.matrix;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import eu.interedition.collatex.graph.VariantGraphVertex;

public class ArchipelagoWithVersions extends Archipelago {
  Logger LOG = LoggerFactory.getLogger(ArchipelagoWithVersions.class);
  private ArrayList<Archipelago> nonConflVersions;
  private final MatchTable table;
  Set<Integer> fixedRows = Sets.newHashSet();
  Set<VariantGraphVertex> fixedVertices = Sets.newHashSet();

  public ArchipelagoWithVersions(MatchTable table) {
    this.table = table;
    setIslands(new ArrayList<Island>());
    nonConflVersions = new ArrayList<Archipelago>();
  }

  public ArchipelagoWithVersions(Island isl, MatchTable table) {
    this(table);
    add(isl);
  }

  @Override
  public void add(Island island) {
    super.add(island);
  }

  @Override
  public ArchipelagoWithVersions copy() {
    ArchipelagoWithVersions result = new ArchipelagoWithVersions(this.table);
    for (Island isl : getIslands()) {
      result.add(new Island(isl));
    }
    return result;
  }

  /*
    * Create a non-conflicting version by simply taken all the islands
    * that do not conflict with each other, largest first. This presuming
    * that Archipelago will have a high value if it contains the largest
    * possible islands
    */
  public Archipelago createNonConflictingVersion(Archipelago archipelago) {
    fixedRows = Sets.newHashSet();
    fixedVertices = Sets.newHashSet();
    Multimap<Integer, Island> islandMultimap = ArrayListMultimap.create();
    for (Island isl : getIslands()) {
      islandMultimap.put(isl.size(), isl);
    }
    List<Integer> keySet = Lists.newArrayList(islandMultimap.keySet());
    Collections.sort(keySet);
    List<Integer> decreasingIslandSizes = Lists.reverse(keySet);
    for (Integer islandSize : decreasingIslandSizes) {
      //      if (islandSize > 0) { // limitation to prevent false transpositions
      List<Island> islands = possibleIslands(islandMultimap.get(islandSize));

      if (islands.size() == 1) {
        addIslandToResult(islands.get(0), archipelago);

      } else if (islands.size() > 1) {
        Set<Island> competingIslands = getCompetingIslands(islands, archipelago);
        Set<Island> competingIslandsOnIdealLine = Sets.newHashSet();
        Set<Island> otherCompetingIslands = Sets.newHashSet();
        for (Island island : competingIslands) {
          Coordinate leftEnd = island.getLeftEnd();
          if (archipelago.getIslandVectors().contains(leftEnd.row - leftEnd.column)) {
            competingIslandsOnIdealLine.add(island);
          } else {
            otherCompetingIslands.add(island);
          }

        }
        Multimap<Double, Island> distanceMap1 = makeDistanceMap(competingIslandsOnIdealLine, archipelago);
        addBestOfCompeting(archipelago, distanceMap1);

        Multimap<Double, Island> distanceMap2 = makeDistanceMap(otherCompetingIslands, archipelago);
        addBestOfCompeting(archipelago, distanceMap2);

        for (Island i : getNonCompetingIslands(islands, competingIslands)) {
          addIslandToResult(i, archipelago);
        }
      }
    }
    //    }
    return archipelago;
  }

  private void addBestOfCompeting(Archipelago archipelago, Multimap<Double, Island> distanceMap1) {
    List<Double> shortestToLongestDistances = shortestToLongestDistances(distanceMap1);
    for (Double d : shortestToLongestDistances) {
      // TODO: find a better way to determine the best choice of island
      for (Island ci : distanceMap1.get(d)) {
        if (islandIsPossible(ci)) {
          addIslandToResult(ci, archipelago);
        }
      }
    }
  }

  private Multimap<Double, Island> makeDistanceMap(Set<Island> competingIslands, Archipelago archipelago) {
    Multimap<Double, Island> distanceMap = ArrayListMultimap.create();
    for (Island isl : competingIslands) {
      distanceMap.put(archipelago.smallestDistance(isl), isl);
    }
    return distanceMap;
  }

  public Archipelago createNonConflictingVersion() {
    return createNonConflictingVersion(new Archipelago());
  }

  public int numOfNonConflConstell() {
    return nonConflVersions.size();
  }

  public ArrayList<Archipelago> getNonConflVersions() {
    return nonConflVersions;
  }

  private List<Double> shortestToLongestDistances(Multimap<Double, Island> distanceMap) {
    List<Double> distances = Lists.newArrayList(distanceMap.keySet());
    Collections.sort(distances);
    return distances;
  }

  private Set<Island> getNonCompetingIslands(List<Island> islands, Set<Island> competingIslands) {
    Set<Island> nonCompetingIslands = Sets.newHashSet(islands);
    nonCompetingIslands.removeAll(competingIslands);
    return nonCompetingIslands;
  }

  private Set<Island> getCompetingIslands(List<Island> islands, Archipelago result) {
    Set<Island> competingIslands = Sets.newHashSet();
    for (int i = 0; i < islands.size(); i++) {
      Island i1 = islands.get(i);
      for (int j = 1; j < islands.size() - i; j++) {
        Island i2 = islands.get(i + j);
        if (result.islandsCompete(i1, i2)) {
          competingIslands.add(i1);
          competingIslands.add(i2);
        }
      }
    }
    return competingIslands;
  }

  @Override
  public boolean islandsCompete(Island i1, Island i2) {
    return false;
  };

  private List<Island> possibleIslands(Collection<Island> islandsOfSize) {
    List<Island> islands = Lists.newArrayList();
    for (Island island : islandsOfSize) {
      if (islandIsPossible(island)) {
        islands.add(island);
      }
    }
    return islands;
  }

  private void addIslandToResult(Island isl, Archipelago result) {
    if (islandIsNoOutlier(result, isl)) {
      LOG.info("adding island: '{}'", isl);
      result.add(isl);
      for (Coordinate coordinate : isl) {
        fixedRows.add(coordinate.row);
        fixedVertices.add(table.at(coordinate.row, coordinate.column));
      }

    } else {
      LOG.info("island: '{}' is an outlier, not added", isl);
    }
  }

  private boolean islandIsPossible(Island island) {
    for (Coordinate coordinate : island) {
      if (fixedRows.contains(coordinate.row) || //
          fixedVertices.contains(table.at(coordinate.row, coordinate.column))) return false;
    }
    return true;
  }

  private boolean islandIsNoOutlier(Archipelago a, Island isl) {
    double smallestDistance = a.smallestDistanceToIdealLine(isl);
    LOG.info("island {}, distance={}", isl, smallestDistance);
    return (!(a.size() > 0 && isl.size() == 1 && smallestDistance >= 5));

    //    if (isl.size() > 1) {
    //      // must limit on size, so not all islands will be outliers
    //      // TODO find the right size to limit on.
    //      return true;
    //
    //    } else {
    //      Coordinate leftEnd = isl.getLeftEnd();
    //      return a.getIslandVectors().contains(leftEnd.row - leftEnd.column);
    //    }
    //
    //    //    if (a.size() == 0) return true;
    //    //    Coordinate leftEnd = isl.getLeftEnd();
    //    //    int v = leftEnd.row - leftEnd.column;
    //    //    Set<Integer> islandVectors = a.getIslandVectors();
    //    //    int minimumDistanceToExistingVectors = 10000;
    //    //    for (Integer iv : islandVectors) {
    //    //      minimumDistanceToExistingVectors = Math.min(minimumDistanceToExistingVectors, Math.abs(v - iv));
    //    //    }
    //    //    return minimumDistanceToExistingVectors <= isl.size();
  }

  //  private boolean islandsAreOnTheSameVector(Island island, Island isl) {
  //    Coordinate leftEnd = island.getLeftEnd();
  //    double x1 = leftEnd.row;
  //    double y1 = leftEnd.column;
  //
  //    Coordinate rightEnd = island.getRightEnd();
  //    double x2 = rightEnd.row;
  //    double y2 = rightEnd.column;
  //
  //    Coordinate leftEndToMatch = isl.getLeftEnd();
  //    double px = leftEndToMatch.row;
  //    double py = leftEndToMatch.column;
  //    double ptLineDistSq = Line2D.ptLineDistSq(x1, y1, x2, y2, px, py);
  //    return ptLineDistSq == 0.0;
  //  }
  //
  //  private double deviation(Archipelago archipelago, Island isl) {
  //    if (archipelago.size() == 0) return 0;
  //
  //    double smallestDistance = archipelago.smallestDistance(isl);
  //    int islandSize = isl.size();
  //    double deviation = smallestDistance / islandSize;
  //    LOG.info("size={}, smallestDistance={}, deviation={}", new Object[] { islandSize, smallestDistance, deviation });
  //    return deviation;
  //  }

  // these methods are only used in tests. remove?
  //  private Integer[] isl2;
  private static final String newLine = System.getProperty("line.separator");

  public void createNonConflictingVersions() {
    boolean debug = false;
    //    PrintWriter logging = null;
    //    File logFile = new File(File.separator + "c:\\logfile.txt");
    //    try {
    //      logging = new PrintWriter(new FileOutputStream(logFile));
    //    } catch (FileNotFoundException e) {
    //      e.printStackTrace();
    //    }

    nonConflVersions = new ArrayList<Archipelago>();
    for (Island island : getIslands()) {
      //      if(tel>22)
      debug = false;
      //        System.out.println("nonConflVersions.size(): "+nonConflVersions.size());
      //        int tel_version = 0;
      //        for(Archipelago arch : nonConflVersions) {
      //          System.out.println("arch version ("+(tel_version++)+"): " + arch);
      //        }
      // TODO
      //      if(tel>22) {
      //        int tel_version = 0;
      //        for(Archipelago arch : nonConflVersions) {
      //          System.out.println("arch version ("+(tel_version++)+"): " + arch);
      //        }
      //        System.exit(1);
      //      }
      if (nonConflVersions.size() == 0) {
        if (debug) System.out.println("nonConflVersions.size()==0");
        Archipelago version = new Archipelago();
        version.add(island);
        nonConflVersions.add(version);
      } else {
        boolean found_one = false;
        ArrayList<Archipelago> new_versions = new ArrayList<Archipelago>();
        int tel_loop = 0;
        for (Archipelago version : nonConflVersions) {
          if (debug) System.out.println("loop 1: " + tel_loop++);
          if (!version.conflictsWith(island)) {
            if (debug) System.out.println("!version.conflictsWith(island)");
            version.add(island);
            found_one = true;
          }
        }
        if (!found_one) {
          if (debug) System.out.println("!found_one");
          // try to find a existing version in which the new island fits
          // after removing some points
          tel_loop = 0;
          for (Archipelago version : nonConflVersions) {
            if (debug) System.out.println("loop 2: " + tel_loop++);
            Island island_copy = new Island(island);
            for (Island isl : version.iterator()) {
              island_copy = island_copy.removePoints(isl);
            }
            if (island_copy.size() > 0) {
              version.add(island_copy);
              found_one = true;
            }
          }
          // create a new version with the new island and (parts of) existing islands
          tel_loop = 0;
          for (Archipelago version : nonConflVersions) {
            if (debug) System.out.println("loop 3: " + tel_loop++);
            Archipelago new_version = new Archipelago();
            new_version.add(island);
            for (Island isl : version.iterator()) {
              Island di = new Island(isl);
              if (debug) System.out.println("di: " + di);
              Island res = di.removePoints(island);
              if (debug) System.out.println("res: " + res);
              if (res.size() > 0) {
                found_one = true;
                new_version.add(res);
              }
            }
            new_versions.add(new_version);
          }
          if (new_versions.size() > 0) {
            tel_loop = 0;
            for (Archipelago arch : new_versions) {
              if (debug) System.out.println("loop 4: " + tel_loop++);
              addVersion(arch);
            }
          }
        }
        if (!found_one) {
          if (debug) System.out.println("!found_one");
          Archipelago version = new Archipelago();
          version.add(island);
          addVersion(version);
        }
      }
    }
  }

  public Archipelago getVersion(int i) {
    try {
      if (nonConflVersions.isEmpty()) createNonConflictingVersions();
      return nonConflVersions.get(i);
    } catch (IndexOutOfBoundsException exc) {
      return null;
    }
  }

  @Deprecated
  public String createXML(MatchTable mat, PrintWriter output) {
    throw new RuntimeException("This class cannot create XML!");
  }

  private void addVersion(Archipelago version) {
    int pos = 0;
    //    int tel_loop = 0;
    //    System.out.println("addVersion - num of versions: "+nonConflVersions.size());
    for (pos = 0; pos < nonConflVersions.size(); pos++) {
      if (version.equals(nonConflVersions.get(pos))) return;
      //      System.out.println("loop 5: "+tel_loop++);
      if (version.value() > nonConflVersions.get(pos).value()) {
        nonConflVersions.add(pos, version);
        return;
      }
    }
    nonConflVersions.add(version);
  }

}