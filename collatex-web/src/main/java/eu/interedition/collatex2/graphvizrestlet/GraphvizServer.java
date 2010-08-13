package eu.interedition.collatex2.graphvizrestlet;

import java.io.IOException;

import javax.management.InstanceAlreadyExistsException;

import org.restlet.Component;
import org.restlet.data.Protocol;

public class GraphvizServer {
  public static void main(String[] args) throws InstanceAlreadyExistsException, IOException {
    try {
      Component component = new Component();
      component.getServers().add(Protocol.HTTP, 8182);
      component.getDefaultHost().attach(new GraphvizRestApplication());
      component.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
