/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.io;

import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;
import org.codehaus.jackson.JsonNode;

public class JsonToken extends SimpleToken {

  private final JsonNode jsonNode;

  public JsonToken(SimpleWitness witness, String content, String normalized, JsonNode jsonNode) {
    super(witness, content, normalized);
    this.jsonNode = jsonNode;
  }

  public JsonNode getJsonNode() {
    return jsonNode;
  }
}
