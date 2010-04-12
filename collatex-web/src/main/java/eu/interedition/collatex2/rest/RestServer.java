package eu.interedition.collatex2.rest;

import org.restlet.Component;
import org.restlet.data.Protocol;

public class RestServer {
  public static void main(final String[] args) {
    try {
      final Component component = new Component();
      component.getServers().add(Protocol.HTTP, 8182);
      component.getDefaultHost().attach(new RestApplication());
      component.start();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
