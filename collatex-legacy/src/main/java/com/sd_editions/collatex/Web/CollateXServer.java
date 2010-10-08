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

package com.sd_editions.collatex.Web;

import org.apache.wicket.protocol.http.WicketServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

public class CollateXServer {
  public static void main(String[] args) throws Exception {
    System.setProperty("org.mortbay.xml.XmlParser.NotValidating", "true");

    Server server = new Server(2000);
    ContextHandlerCollection contexts = new ContextHandlerCollection();
    server.setHandler(contexts);

    ContextHandler files = new ContextHandler("/site");
    String curDir = System.getProperty("user.dir");
    files.setResourceBase(curDir + "/site");
    ResourceHandler resource_handler = new ResourceHandler();
    files.addHandler(resource_handler);
    contexts.addHandler(files);

    Context root = new Context(contexts, "/", Context.SESSIONS);
    ServletHolder holder = new ServletHolder(new WicketServlet());
    holder.setInitParameter("applicationClassName", "com.sd_editions.collatex.Web.CollateXApplication");
    root.addServlet(holder, "/*");

    server.start();
  }

}
