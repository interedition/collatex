package eu.interedition.collatex.alignment.multiple_witness.segments;

public class SegmentColumn {

  private final Segment _segment;

  public SegmentColumn(Segment segment) {
    this._segment = segment;
  }

  // TODO: add other segments!
  @Override
  public String toString() {
    return _segment.toString();
  }

  public void addToSuperbase(SegmentSuperbase superbase) {
    superbase.addSegment(_segment, this);
  }
}
