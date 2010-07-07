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

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.AlignmentTableCreator;
import eu.interedition.collatex.input.WitnessSet;

public class SegmentationResource extends ServerResource {

  public SegmentationResource() {
    getVariants().add(new Variant(MediaType.TEXT_HTML));
    getVariants().add(new Variant(MediaType.TEXT_PLAIN));
  }

  @Override
  public Representation get(Variant variant) throws ResourceException {
    // String witnessString = getQuery().getFirstValue("witness");
    // System.err.println("!!" + witnessString);
    String[] witnessStrings = getQuery().getValuesArray("witness");
    WitnessSet set = WitnessSet.createWitnessSet(witnessStrings);
    AlignmentTable2 alignmentTable = AlignmentTableCreator.createAlignmentTable(set);
    // TODO make a visitor out of this! (this is actually tei parallel
    // segmentation)
    String xml = alignmentTable.toXML();
    // JSONObjectTableVisitor visitor = new JSONObjectTableVisitor();
    // alignmentTable.accept(visitor);
    // JSONObject jsonObject = visitor.getJSONObject();
    // Representation representation = new JsonLibRepresentation(jsonObject);
    Representation representation = new StringRepresentation(xml, MediaType.APPLICATION_XML);
    // Representation representation = null;
    return representation;
  }

}
