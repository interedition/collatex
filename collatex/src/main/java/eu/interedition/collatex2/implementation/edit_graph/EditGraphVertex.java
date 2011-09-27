package eu.interedition.collatex2.implementation.edit_graph;

import com.google.common.base.Objects;

import eu.interedition.collatex2.interfaces.INormalizedToken;

// This class represents vertices in the EditGraph
// This class is implemented as an immutable value object
// private fields are final
// toString(), hashCode() and equals methods are overridden
public class EditGraphVertex {
  private final INormalizedToken baseToken;
  private final INormalizedToken witnessToken;
  
  public EditGraphVertex(INormalizedToken witnessToken, INormalizedToken baseToken) {
    this.baseToken = baseToken;
    this.witnessToken = witnessToken;
  }

  public INormalizedToken getBaseToken() {
    return baseToken;
  }

  @Override
  public String toString() {
    if (witnessToken==null||baseToken==null) {
      return "start/end vertex";
    }
    return witnessToken.toString()+"->"+baseToken.toString();
  }
  
  @Override
  public int hashCode() {
    int hc = Objects.hashCode(baseToken, witnessToken);
//    System.out.println("hashcode called on: "+this.toString()+":"+hc);
    return hc;
  }
  
  @Override
  public boolean equals(final Object obj) {
    //System.out.println(this.toString()+" comparing with "+obj.toString());
    if (this==obj) {
      return true;
    }
    if (obj instanceof EditGraphVertex) {
      final EditGraphVertex vertex = (EditGraphVertex) obj;
      boolean result = Objects.equal(baseToken, vertex.baseToken);
      result = result && Objects.equal(witnessToken, vertex.witnessToken);
      return result;
    }
    return false;
  }
}
