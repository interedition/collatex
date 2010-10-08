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

package eu.interedition.collatex.rest.examples;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class ExampleResource extends ServerResource {

  public ExampleResource() {
    getVariants().add(new Variant(MediaType.TEXT_HTML));
    getVariants().add(new Variant(MediaType.TEXT_PLAIN));
  }

  @Override
  public Representation get(Variant variant) throws ResourceException {
    return null;
    //      try {
    //        String id = (String) getRequest().getAttributes().get("name");
    //        Database database = Database.getInstance();
    //        String name = database.getName(id);
    //        if (variant != null && variant.getMediaType() == MediaType.TEXT_PLAIN) {
    //          return new StringRepresentation(name, MediaType.TEXT_PLAIN);
    //        } else {
    //          StringBuilder builder = new StringBuilder();
    //          builder.append("<html><body>");
    //          builder.append("<h1>Author</h1>\n");
    //          builder.append("Name: ");
    //          builder.append(name);
    //          builder.append("</body></html>");
    //          return new StringRepresentation(builder.toString(), MediaType.TEXT_HTML);
    //        }
    //      } catch (SQLException e) {
    //        throw new ResourceException(404);
    //      }
  }

}
