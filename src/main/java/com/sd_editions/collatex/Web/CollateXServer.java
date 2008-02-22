package com.sd_editions.collatex.Web;

import org.apache.wicket.protocol.http.WicketServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

public class CollateXServer {
  public static void main(String[] args) throws Exception {
    System.setProperty("org.mortbay.xml.XmlParser.NotValidating", "true");
    Server server = new Server(8080);
    Context root = new Context(server, "/", Context.SESSIONS);
    ServletHolder holder = new ServletHolder(new WicketServlet());
    holder.setInitParameter("applicationClassName", "com.sd_editions.collatex.Web.CollateXApplication");
    root.addServlet(holder, "/*");
    server.start();
  }

}
