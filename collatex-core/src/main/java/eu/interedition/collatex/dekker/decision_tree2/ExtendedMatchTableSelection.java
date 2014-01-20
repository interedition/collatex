package eu.interedition.collatex.dekker.decision_tree2;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.dekker.matrix.MatchTable;
import eu.interedition.collatex.dekker.matrix.MatchTableSelection;

/*
 * Extended MatchTableSelection class,
 * which allows the caller to select vectors from the MatchTable 
 * based on different requirements compared to the default MatchTableSelection
 * class, which always returns the largest vectors.
 * 
 * @author: Ronald Haentjens Dekker
 */
public class ExtendedMatchTableSelection extends MatchTableSelection {
  private final Set<Island> possibleIslands;
  private final Set<Island> transposedIslands;
  boolean skippedIslands;
  // temporary measure to track the results
  private final List<String> log;
  
  public ExtendedMatchTableSelection(MatchTable table) {
    super(table);
    this.possibleIslands = table.getIslands();
    this.transposedIslands = Sets.newHashSet();
    this.skippedIslands = false;
    this.log = Lists.newArrayList();
  }
  
  //NOTE: copy constructor
  public ExtendedMatchTableSelection(ExtendedMatchTableSelection orig) {
    super(orig);
    this.possibleIslands = Sets.newHashSet(orig.possibleIslands);
    this.transposedIslands = Sets.newHashSet(orig.transposedIslands);
    this.skippedIslands = orig.skippedIslands;
    this.log = Lists.newArrayList(orig.log);
  }

  public DecisionTreeNode selectFirstVectorFromGraph() {
    Island i = getFirstVectorFromGraph();
    selectIsland(i);
    log.add(String.format("sel g %s", i));
    return new DecisionTreeNode(this);
  }

  public DecisionTreeNode selectFirstVectorFromWitness() {
    Island i = getFirstVectorFromWitness();
    selectIsland(i);
    log.add(String.format("sel w %s", i));
    return new DecisionTreeNode(this);
  }

  public DecisionTreeNode skipFirstVectorFromGraph() {
    skippedIslands = true;
    Island first = getFirstVectorFromGraph();
    removeIslandFromPossibilities(first);
    log.add(String.format("skip g %s", first));
    return new DecisionTreeNode(this);
  }

  public DecisionTreeNode skipFirstVectorFromWitness() {
    skippedIslands = true;
    Island first = getFirstVectorFromWitness();
    removeIslandFromPossibilities(first);
    log.add(String.format("skip w %s", first));
    return new DecisionTreeNode(this);
  }

  public DecisionTreeNode selectFirstVectorGraphTransposeWitness() {
    Island firstVectorFromGraph = getFirstVectorFromGraph();
    // Find that vector in the witness
    // as long as you can't find that vector
    // transpose the vectors in the witness
    Island witness = getFirstVectorFromWitness();
    do {
      log.add(String.format("transposed w %s", witness));
      transposeVector(witness);
      witness = getFirstVectorFromWitness();
    } while (witness != firstVectorFromGraph);
    return selectFirstVectorFromGraph();
  }
  
  public DecisionTreeNode selectFirstVectorWitnessTransposeGraph() {
    Island firstVectorFromWitness = getFirstVectorFromWitness();
    // Find that vector in the graph
    // as long as you can't find that vector
    // transpose the vectors in the graph
    Island graph = getFirstVectorFromGraph();
    do {
      log.add(String.format("transposed g %s", graph));
      transposeVector(graph);
      graph = getFirstVectorFromGraph();
    } while (graph != firstVectorFromWitness);
    return selectFirstVectorFromWitness();
  }

  public Island getFirstVectorFromGraph() {
    Preconditions.checkArgument(!possibleIslands.isEmpty(), "The possible islands is empty. Check before calling!");
    Comparator<Island> comp = new Comparator<Island> () {
      @Override
      public int compare(Island arg0, Island arg1) {
        // first sort on column
        // TODO: second sort on size
        int result = arg0.getLeftEnd().getColumn() - arg1.getLeftEnd().getColumn();
        if (result==0) {
          result = arg0.getLeftEnd().getRow() - arg1.getLeftEnd().getRow();
        }
        Preconditions.checkArgument(result!=0, "Islands are not sortable! %s %s", arg0, arg1);
        return result;
      }
    };
    List<Island> vectorsSortedOnGraphOrder = Lists.newArrayList(possibleIslands);
    Collections.sort(vectorsSortedOnGraphOrder, comp);
    return vectorsSortedOnGraphOrder.get(0);
  }
  
  public Island getFirstVectorFromWitness() {
    Preconditions.checkArgument(!possibleIslands.isEmpty(), "The possible islands is empty. Check before calling!");    
    Comparator<Island> comp = new Comparator<Island> () {
      @Override
      public int compare(Island arg0, Island arg1) {
        // first sort on row
        // TODO: second sort on size
        int result = arg0.getLeftEnd().getRow() - arg1.getLeftEnd().getRow();
        if (result==0) {
          result = arg0.getLeftEnd().getColumn() - arg1.getLeftEnd().getColumn();
        }
        Preconditions.checkArgument(result!=0, "Islands are not sortable! %s %s", arg0, arg1);
        return result;
      }
    };
    List<Island> vectorsSortedOnWitnessOrder = Lists.newArrayList(possibleIslands);
    Collections.sort(vectorsSortedOnWitnessOrder, comp);
    return vectorsSortedOnWitnessOrder.get(0);
  }

  @Override
  public void selectIsland(Island isl) {
    possibleIslands.remove(isl);
    super.selectIsland(isl);
    removeOrSplitImpossibleIslands();
    //Note: this does not call the super removeOrSplitImpossibleIslands
    //this causes problems; 
    //We have to remove the inheritance here
  }

  public void transposeVector(Island island) {
    transposedIslands.add(island);
    removeIslandFromPossibilities(island);
  }  
  
  @Override
  public void removeIslandFromPossibilities(Island island) {
    possibleIslands.remove(island);
    super.removeIslandFromPossibilities(island);
  }
  
  public int sizeOfGraph() {
    return table.horizontalSize();
  }
  
  public String log() {
    return Joiner.on("; ").join(log);
  }
  
  //TODO: this can be done faster by only checking the possible islands
  //against the newly selected island!
  //Note: implementation of this method differs from superclass.
  private void removeOrSplitImpossibleIslands() {
    Set<Island> impossibleIslands = Sets.newHashSet();
    for (Island island : possibleIslands) {
      if (!isIslandPossibleCandidate(island)) {
        impossibleIslands.add(island);
      }
    }
    for (Island island : impossibleIslands) {
      possibleIslands.remove(island);
      Island splitIsland = new Island(island);
      super.removeConflictingEndCoordinates(splitIsland);
      if (splitIsland.size() > 0) {
        possibleIslands.add(splitIsland);
      }
    }
  }
  
  //TODO: not nice!
  //TODO: see comment on selectIsland(method)
  @Override
  public List<Island> getPossibleIslands() {
    return Lists.newArrayList(possibleIslands);
  }

  public Set<Island> getTransposedIslands() {
    return transposedIslands;
  }



}
