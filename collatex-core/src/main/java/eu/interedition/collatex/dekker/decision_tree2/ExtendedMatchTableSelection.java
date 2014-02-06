package eu.interedition.collatex.dekker.decision_tree2;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.dekker.matrix.Coordinate;
import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.dekker.matrix.MatchTable;
import eu.interedition.collatex.dekker.matrix.MatchTableSelection;
import eu.interedition.collatex.simple.SimpleToken;

/*
 * Extended MatchTableSelection class,
 * which allows the caller to select vectors from the MatchTable 
 * based on different requirements compared to the default MatchTableSelection
 * class, which always returns the largest vectors.
 * 
 * @author: Ronald Haentjens Dekker
 */
public class ExtendedMatchTableSelection extends MatchTableSelection {
  protected final static Logger LOG = Logger.getLogger(ExtendedMatchTableSelection.class.getName());
  private final Set<Island> possibleIslands;
  
  public ExtendedMatchTableSelection(MatchTable table) {
    super(table);
    this.possibleIslands = table.getIslands();
  }
  
  //NOTE: copy constructor
  public ExtendedMatchTableSelection(ExtendedMatchTableSelection orig) {
    super(orig);
    this.possibleIslands = Sets.newHashSet(orig.possibleIslands);
  }

  public Island getFirstVectorFromGraph() {
    List<Island> vectorsSortedOnGraphOrder = sortVectorsOnGraphOrder();
    return vectorsSortedOnGraphOrder.get(0);
  }

  private List<Island> sortVectorsOnGraphOrder() {
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
    return vectorsSortedOnGraphOrder;
  }
  
  public Island getFirstVectorFromWitness() {
    List<Island> vectorsSortedOnWitnessOrder = sortVectorsOnWitnessOrder();
    return vectorsSortedOnWitnessOrder.get(0);
  }

  private List<Island> sortVectorsOnWitnessOrder() {
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
    return vectorsSortedOnWitnessOrder;
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

  @Override
  public void removeIslandFromPossibilities(Island island) {
    possibleIslands.remove(island);
    super.removeIslandFromPossibilities(island);
  }
  
  public int sizeOfGraph() {
    return table.horizontalSize();
  }
  
  public int sizeOfWitness() {
    return table.verticalSize();
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

  public ExtendedMatchTableSelection copy() {
    return new ExtendedMatchTableSelection(this);
  }

  public boolean isFinished() {
    return getPossibleIslands().isEmpty();
  }

  public MatchTable getTable() {
    return table;
  }

  public void debugPossibleVectors() {
    List<Island> vectorsOnGraphOrder = sortVectorsOnGraphOrder();
    int numberOfVectorsToShow = Math.min(vectorsOnGraphOrder.size(), 5);
    List<Island> subList = vectorsOnGraphOrder.subList(0, numberOfVectorsToShow-1);
    StringBuilder builder = new StringBuilder();
    builder.append("Graph: [");
    for (Island i : subList) {
      builder.append(token(i));
      builder.append(" (");
      builder.append(i.toString());
      builder.append(")");
      builder.append(", ");
    }
    builder.append(" ]");
    LOG.finer(builder.toString());
    List<Island> vectorsOnWitnessOrder = sortVectorsOnWitnessOrder();
    numberOfVectorsToShow = Math.min(vectorsOnWitnessOrder.size(), 5);
    subList = vectorsOnWitnessOrder.subList(0, numberOfVectorsToShow-1);
    builder = new StringBuilder();
    builder.append("Witness: [");
    for (Island i : subList) {
      builder.append(token(i));
      builder.append(" (");
      builder.append(i.toString());
      builder.append(")");
      builder.append(", ");
    }
    builder.append(" ]");
    LOG.finer(builder.toString());
  }

  private String token(Island i) {
    List<Token> witnessTokens = Lists.newArrayList();
    for (Coordinate c : i) {
      witnessTokens.add(table.tokenAt(c.getRow(), c.getColumn()));
    }
    String tokenString = SimpleToken.toString(witnessTokens);
    return tokenString;
  }
}
