package eu.interedition.collatex.matching;

public class SegmentPosition {

  public final String witnessId;
  public final Integer position;

  public SegmentPosition(String witnessId1, Integer position1) {
    this.witnessId = witnessId1;
    this.position = position1;
  }

  @Override
  public String toString() {
    return witnessId + ":" + position;
  }

  @SuppressWarnings("boxing")
  public SegmentPosition nextSegmentPosition() {
    return new SegmentPosition(witnessId, position + 1);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SegmentPosition)) return false;
    SegmentPosition segmentPosition = (SegmentPosition) obj;
    return segmentPosition.witnessId.equals(this.witnessId) && segmentPosition.position.equals(this.position);
  }

  @Override
  public int hashCode() {
    return 10 * witnessId.hashCode() + position.hashCode();
  }
}
