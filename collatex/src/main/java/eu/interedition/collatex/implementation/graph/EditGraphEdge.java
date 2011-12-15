package eu.interedition.collatex.implementation.graph;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import org.neo4j.graphdb.Relationship;

import java.util.SortedSet;

import static eu.interedition.collatex.implementation.graph.GraphRelationshipType.PATH;

public class EditGraphEdge extends GraphEdge<EditGraph, EditGraphVertex> {
  static final String EDIT_OPERATION_KEY = "editOperation";
  static final String SCORE_KEY = "score";
  static final String SHORTEST_PATH_IDS_KEY = "pathIds";

  public EditGraphEdge(EditGraph graph, Relationship relationship) {
    super(graph, relationship);
  }

  public EditGraphEdge(EditGraph graph, EditGraphVertex from, EditGraphVertex to, EditOperation operation, int score) {
    super(graph, from.getNode().createRelationshipTo(to.getNode(), PATH));
    relationship.setProperty(EDIT_OPERATION_KEY, operation.ordinal());
    relationship.setProperty(SCORE_KEY, score);
  }

  public EditOperation getEditOperation() {
    return EditOperation.values()[(Integer) relationship.getProperty(EDIT_OPERATION_KEY)];
  }


  public int getScore() {
    return (Integer) relationship.getProperty(SCORE_KEY);
  }

  public SortedSet<Integer> getShortestPathIds() {
    return Sets.newTreeSet(Ints.asList(((int[]) relationship.getProperty(SHORTEST_PATH_IDS_KEY, new int[0]))));
  }
  
  public void addShortestPathId(int path) {
    final SortedSet<Integer> paths = getShortestPathIds();
    paths.add(path);
    relationship.setProperty(SHORTEST_PATH_IDS_KEY, paths.toArray(new Integer[paths.size()]));
  }

  @Override
  public String toString() {
    return "(" + from() + ")->(" + to() + "):" + getEditOperation();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(from(), to(), getEditOperation());
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj != null && obj instanceof EditGraphEdge) {
      final EditGraphEdge edge = (EditGraphEdge) obj;

      if (!Objects.equal(from(), edge.from())) {
        return false;
      }
      if (!Objects.equal(to(), edge.to())) {
        return false;
      }

      return Objects.equal(getEditOperation(), edge.getEditOperation());
    }
    return super.equals(obj);
  }

  public static Function<Relationship, EditGraphEdge> createWrapper(final EditGraph graph) {
    return new Function<Relationship, EditGraphEdge>() {
      @Override
      public EditGraphEdge apply(Relationship input) {
        return new EditGraphEdge(graph, input);
      }
    };
  }
}
