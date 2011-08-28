/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.web;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import eu.interedition.collatex2.implementation.containers.witness.Witness;
import eu.interedition.collatex2.interfaces.INormalizedToken;

public class ApiWitness extends Witness {
  private String content;

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

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
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
