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

import java.io.IOException;

import javax.management.InstanceAlreadyExistsException;

import org.restlet.Component;
import org.restlet.data.Protocol;

public class RestServer {
  public static void main(String[] args) throws InstanceAlreadyExistsException, IOException {
    //    if (args.length > 0 && "-i".equals(args[0])) {
    //      validate(args.length >= 2, "-i requires parameter");
    //      new ConfigurationLoader().load(args[1]);
    //    } else {
    //      new ConfigurationLoader().load();
    //    }

    try {
      Component component = new Component();
      component.getServers().add(Protocol.HTTP, 8182);
      component.getDefaultHost().attach(new RestApplication());
      component.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
