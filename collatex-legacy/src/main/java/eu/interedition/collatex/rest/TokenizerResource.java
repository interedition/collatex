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

package eu.interedition.collatex.rest;

import net.sf.json.JSONArray;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import eu.interedition.collatex.input.WitnessSet;
import eu.interedition.collatex.input.visitors.JSONObjectVisitor;
import eu.interedition.collatex2.rest.output.JsonLibRepresentation;

public class TokenizerResource extends ServerResource {

  public TokenizerResource() {
    getVariants().add(new Variant(MediaType.TEXT_HTML));
    getVariants().add(new Variant(MediaType.TEXT_PLAIN));
  }

  @Override
  public Representation get(Variant variant) throws ResourceException {
    String[] witnessStrings = getQuery().getValuesArray("witness");
    WitnessSet set = WitnessSet.createWitnessSet(witnessStrings);
    JSONObjectVisitor visitor = new JSONObjectVisitor();
    set.accept(visitor);
    JSONArray jsonArray = visitor.getJsonArray();
    Representation representation = new JsonLibRepresentation(jsonArray);
    return representation;
  }

}
