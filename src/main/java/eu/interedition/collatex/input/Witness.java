package eu.interedition.collatex.input;

import eu.interedition.collatex.input.visitors.JSONObjectVisitor;

public class Witness {

  private final Segment _segment;

  //  private String _id;

  public Witness(Segment segment) {
    this._segment = segment;
  }

  //  public String getId() {
  //    return _id;
  //  }

  public void accept(JSONObjectVisitor visitor) {
    visitor.visitWitness(this);
    getFirstSegment().accept(visitor);
  }

  public Segment getFirstSegment() {
    return _segment;
  }

}
