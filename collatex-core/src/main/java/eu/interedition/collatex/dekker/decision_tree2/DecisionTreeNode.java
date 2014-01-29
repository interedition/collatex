package eu.interedition.collatex.dekker.decision_tree2;

import eu.interedition.collatex.dekker.matrix.Island;

/*
 * DecisionTreeNode class
 * Note: not a value object.. on the basis of the selected vector
 * nodes are not uniquely identifiable
 * 
 * @Author: Ronald Haentjens Dekker
 */
public class DecisionTreeNode {
  //to be expanded with transposed vectors etc.
  private Island selected;

  public Island getSelected() {
    return selected;
  }

  public void setSelected(Island selected) {
    this.selected = selected;
  }
 }
