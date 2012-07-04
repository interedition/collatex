package eu.interedition.collatex.dekker.matrix;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class ArchipelagoWithVersions extends Archipelago {
  Logger LOG = LoggerFactory.getLogger(ArchipelagoWithVersions.class);
  private ArrayList<Archipelago> nonConflVersions;

  public ArchipelagoWithVersions() {
    setIslands(new ArrayList<Island>());
    nonConflVersions = new ArrayList<Archipelago>();
  }

  public ArchipelagoWithVersions(Island isl) {
    setIslands(new ArrayList<Island>());
    nonConflVersions = new ArrayList<Archipelago>();
    add(isl);
  }

  @Override
  public void add(Island island) {
    super.add(island);
  }

  @Override
  public ArchipelagoWithVersions copy() {
    ArchipelagoWithVersions result = new ArchipelagoWithVersions();
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
  public Archipelago createNonConflictingVersion(Archipelago result) {
    Map<Integer, Integer> fixedIslandCoordinates = Maps.newHashMap();
    Multimap<Integer, Island> islandMultimap = ArrayListMultimap.create();
    for (Island isl : getIslands()) {
      islandMultimap.put(isl.size(), isl);
    }
    List<Integer> keySet = Lists.newArrayList(islandMultimap.keySet());
    Collections.sort(keySet);
    List<Integer> decreasingIslandSizes = Lists.reverse(keySet);
    for (Integer islandSize : decreasingIslandSizes) {
      if (islandSize > 0) { // limitation to prevent false transpositions
        List<Island> islands = possibleIslands(fixedIslandCoordinates, islandMultimap, islandSize);

        if (islands.size() == 1) {
          fixedIslandCoordinates = addIslandToResult(fixedIslandCoordinates, result, islands.get(0));

        } else if (islands.size() > 1) {
          Set<Island> competingIslands = getCompetingIslands(result, islands);

          Multimap<Double, Island> distanceMap = ArrayListMultimap.create();
          for (Island isl : competingIslands) {
            distanceMap.put(result.smallestDistance(isl), isl);
          }
          //          LOG.info("distanceMap = {}", distanceMap);

          for (Double d : shortestToLongestDistances(distanceMap)) {
            // TODO: find a better way to determine the best choice of island
            for (Island ci : distanceMap.get(d)) {
              if (islandIsPossible(ci, fixedIslandCoordinates)) {
                fixedIslandCoordinates = addIslandToResult(fixedIslandCoordinates, result, ci);
              }
            }
          }

          for (Island i : getNonCompetingIslands(islands, competingIslands)) {
            fixedIslandCoordinates = addIslandToResult(fixedIslandCoordinates, result, i);
          }
        }
      }
    }
    return result;
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

  private Set<Island> getCompetingIslands(Archipelago result, List<Island> islands) {
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

  private List<Island> possibleIslands(Map<Integer, Integer> fixedIslandCoordinates, Multimap<Integer, Island> islandMultimap, Integer size) {
    List<Island> islands = Lists.newArrayList();
    for (Island island : islandMultimap.get(size)) {
      if (islandIsPossible(island, fixedIslandCoordinates)) {
        islands.add(island);
      }
    }
    return islands;
  }

  private Map<Integer, Integer> addIslandToResult(Map<Integer, Integer> fixedIslandCoordinates, Archipelago result, Island isl) {
    //    LOG.info("adding island: '{}'", isl);
    result.add(isl);
    return fixIslandCoordinates(isl, fixedIslandCoordinates);
  }

  private Map<Integer, Integer> fixIslandCoordinates(Island isl, Map<Integer, Integer> fixedIslandCoordinates) {
    for (Coordinate coordinates : isl) {
      fixedIslandCoordinates.put(coordinates.row, coordinates.column);
    }
    return fixedIslandCoordinates;
  }

  private boolean islandIsPossible(Island island, Map<Integer, Integer> fixedIslandCoordinates) {
    boolean possible = true;
    for (Coordinate coordinates : island) {
      if (fixedIslandCoordinates.containsKey(coordinates.row) || //
          fixedIslandCoordinates.containsValue(coordinates.column)) return false;
    }
    return possible;
  }

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
    //    int tel_version = 0;
    //    for(Archipelago arch : nonConflVersions) {
    //      logging.println("arch version ("+(tel_version++)+"): " + arch);
    //    }
    //    tel_version = 0;
    //    for(Archipelago arch : nonConflVersions) {
    //      logging.println("version "+(tel_version++)+": " + arch.value());
    //    }
    //    logging.close();
  }

  public Archipelago getVersion(int i) {
    try {
      if (nonConflVersions.isEmpty()) createNonConflictingVersions();
      return nonConflVersions.get(i);
    } catch (IndexOutOfBoundsException exc) {
      return null;
    }
  }

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