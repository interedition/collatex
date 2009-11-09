package eu.interedition.collatex.visualization;

import com.sd_editions.collatex.match.views.ModificationVisitor;

//TODO: move to alignment modification subpackage?
public abstract class Modification {

  // TODO: make abstract!
  public void accept(ModificationVisitor modificationVisitor) {}

}
