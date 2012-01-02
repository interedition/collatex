package eu.interedition.collatex.lab;

import eu.interedition.collatex.graph.EditOperation;

import eu.interedition.collatex.graph.Score;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class EditGraphEdgeModel {
  private final EditOperation editOperation;
  private final SortedSet<Integer> paths;
  private final Score score;

  public EditGraphEdgeModel(EditOperation editOperation, SortedSet<Integer> paths, Score score) {
    this.editOperation = editOperation;
    this.paths = paths;
    this.score = score;
  }

  public EditOperation getEditOperation() {
    return editOperation;
  }

  public SortedSet<Integer> getPaths() {
    return paths;
  }

  public Score getScore() {
    return score;
  }
}
