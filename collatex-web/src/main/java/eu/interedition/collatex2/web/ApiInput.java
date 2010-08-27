package eu.interedition.collatex2.web;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

public class ApiInput {

  @JsonProperty
  @JsonDeserialize(contentAs = ApiWitness.class)
  private List<ApiWitness> witnesses;

  public List<ApiWitness> getWitnesses() {
    return witnesses;
  }

  public void setWitnesses(List<ApiWitness> witnesses) {
    this.witnesses = witnesses;
  }
}
