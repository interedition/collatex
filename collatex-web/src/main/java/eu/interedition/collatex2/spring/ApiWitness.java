package eu.interedition.collatex2.spring;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import eu.interedition.collatex2.input.NormalizedWitness;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class ApiWitness extends NormalizedWitness {

  public ApiWitness() {
    super();
  }

  @Override
  @JsonProperty("id")
  public String getSigil() {
    return super.getSigil();
  }

  @Override
  @JsonProperty("id")
  public void setSigil(String sigil) {
    super.setSigil(sigil);
  }

  @Override
  @JsonDeserialize(contentAs = ApiToken.class)
  public void setTokens(List<INormalizedToken> tokens) {
    super.setTokens(tokens);
  }

  @SuppressWarnings("unchecked")
  @JsonIgnore
  public List<ApiToken> getApiTokens() {
    return (List) getTokens();
  }
}
