package eu.interedition.collatex.dekker.decision_tree2;

import java.util.Set;

import com.google.common.collect.Sets;

import eu.interedition.collatex.dekker.matrix.Island;

/*
 * DecisionTreeNode class
 * Note: not a value object.. on the basis of the selected vector
 * nodes are not uniquely identifiable
 * 
 * @Author: Ronald Haentjens Dekker
 */
public class DecisionTreeNode {
  private Island lastSelected;
  private Set<Island> transposedVectors = Sets.newHashSet();

  public Island getLastSelected() {
    return lastSelected;
  }

  public void setLastSelected(Island selected) {
    this.lastSelected = selected;
  }

  public void addTransposed(Island transposed) {
    transposedVectors.add(transposed);
  }
}
