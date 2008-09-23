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
