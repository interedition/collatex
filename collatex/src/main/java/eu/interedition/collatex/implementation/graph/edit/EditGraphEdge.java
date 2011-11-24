package eu.interedition.collatex.implementation.graph.edit;

import com.google.common.base.Objects;

//This class represents edges in the EditGraph
//This class is implemented as an immutable value object
//private fields are final
//toString(), hashCode() and equals methods are overridden
public class EditGraphEdge {

  private final EditGraphVertex sourceVertex;
  private final EditGraphVertex targetVertex;
  private final EditOperation operation;
  private final int score;

  public EditGraphEdge(EditGraphVertex source, EditGraphVertex target, EditOperation operation, int score) {
    this.sourceVertex = source;
    this.targetVertex = target;
    this.operation = operation;
    this.score = score;
  }

  public EditGraphVertex getSourceVertex() {
    return sourceVertex;
  }

  public EditGraphVertex getTargetVertex() {
    return targetVertex;
  }

  public EditOperation getEditOperation() {
    return operation;
  }

  Integer getScore() {
    return score;
  }

  @Override
  public String toString() {
    return "("+sourceVertex+")->("+targetVertex+"):"+operation;
  }
  
  @Override
  public int hashCode() {
    int hc = Objects.hashCode(sourceVertex, targetVertex, operation);
//    System.out.println("hashcode called on: "+this.toString()+":"+hc);
    return hc;
  }
  
  @Override
  public boolean equals(final Object obj) {
    //System.out.println(this.toString()+" comparing with "+obj.toString());
    if (this==obj) {
      return true;
    }
    if (obj instanceof EditGraphEdge) {
      final EditGraphEdge edge = (EditGraphEdge) obj;
      boolean result = Objects.equal(sourceVertex, edge.sourceVertex); 
      result = result && Objects.equal(targetVertex, edge.targetVertex);
      result = result && Objects.equal(operation, edge.operation);
      return result;
    }
    return false;
  }

}
