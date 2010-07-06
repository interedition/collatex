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

package eu.interedition.collatex2.rest.output;

import java.io.IOException;
import java.io.Writer;

import net.sf.json.JSON;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.WriterRepresentation;

public class JsonLibRepresentation extends WriterRepresentation {

  private final JSON json;

  public JsonLibRepresentation(net.sf.json.JSON json) {
    super(MediaType.APPLICATION_JSON);
    setCharacterSet(CharacterSet.UTF_8);
    this.json = json;
  }

  @Override
  public void write(Writer writer) throws IOException {
    json.write(writer);
  }
}
