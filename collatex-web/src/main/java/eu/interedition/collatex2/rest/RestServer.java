package eu.interedition.collatex2.rest;

import org.restlet.Component;
import org.restlet.data.Protocol;

public class RestServer {
  public static void main(final String[] args) {
    try {
      int serverPort = 8182;

      if (args.length > 0) {
        try {
          serverPort = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
          e.printStackTrace();
        }
      }
      System.out.println(String.format("Running CollateX server on port %d", serverPort));
      final Component component = new Component();
      component.getServers().add(Protocol.HTTP, serverPort);
      component.getDefaultHost().attach(new RestApplication());
      component.start();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
