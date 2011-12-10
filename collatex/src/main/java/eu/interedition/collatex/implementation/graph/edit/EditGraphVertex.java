package eu.interedition.collatex.implementation.graph.edit;

import com.google.common.base.Objects;

import eu.interedition.collatex.interfaces.Token;

// This class represents vertices in the EditGraph
// This class is implemented as an immutable value object
// private fields are final
// toString(), hashCode() and equals methods are overridden
public class EditGraphVertex {
  private final Token baseToken;
  private final Token witnessToken;
  private int weight = -1;

  public EditGraphVertex(Token witnessToken, Token baseToken) {
    this.baseToken = baseToken;
    this.witnessToken = witnessToken;
  }

  public Token getBaseToken() {
    return baseToken;
  }

  @Override
  public String toString() {
    if (getWitnessToken() == null || baseToken == null) {
      return "start/end vertex";
    }
    String string = getWitnessToken().toString() + "->" + baseToken.toString();
    if (weight > 0) {
      string += " (weight:" + String.valueOf(this.weight) + ")";
    }
    return string;
  }

  @Override
  public int hashCode() {
    int hc = Objects.hashCode(baseToken, getWitnessToken());
    //    System.out.println("hashcode called on: "+this.toString()+":"+hc);
    return hc;
  }

  @Override
  public boolean equals(final Object obj) {
    //System.out.println(this.toString()+" comparing with "+obj.toString());
    if (this == obj) {
      return true;
    }
    if (obj instanceof EditGraphVertex) {
      final EditGraphVertex vertex = (EditGraphVertex) obj;
      boolean result = Objects.equal(baseToken, vertex.baseToken);
      result = result && Objects.equal(getWitnessToken(), vertex.getWitnessToken());
      return result;
    }
    return false;
  }

  public Token getWitnessToken() {
    return witnessToken;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public int getWeight() {
    return weight;
  }
}
