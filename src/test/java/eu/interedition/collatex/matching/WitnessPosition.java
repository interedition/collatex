package eu.interedition.collatex.matching;

public class WitnessPosition {

  public final String witnessId;
  public final Integer position;

  public WitnessPosition(String witnessId1, Integer position1) {
    this.witnessId = witnessId1;
    this.position = position1;
  }

  @Override
  public String toString() {
    return witnessId + ":" + position;
  }

  @SuppressWarnings("boxing")
  public WitnessPosition nextWitnessPosition() {
    return new WitnessPosition(witnessId, position + 1);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof WitnessPosition)) return false;
    WitnessPosition witnessPosition = (WitnessPosition) obj;
    return witnessPosition.witnessId.equals(this.witnessId) && witnessPosition.position.equals(this.position);
  }

  @Override
  public int hashCode() {
    return 10 * witnessId.hashCode() + position.hashCode();
  }
}
