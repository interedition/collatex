package eu.interedition.collatex2.rest;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.interedition.collatex2.implementation.indexing.AlignmentTableIndex;

public class RestServer {
  private static Logger logger = LoggerFactory.getLogger(AlignmentTableIndex.class);
  
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
      logger.info(String.format("Running CollateX server on port %d", serverPort));
      final Component component = new Component();
      component.getServers().add(Protocol.HTTP, serverPort);
      component.getDefaultHost().attach(new RestApplication());
      component.start();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
