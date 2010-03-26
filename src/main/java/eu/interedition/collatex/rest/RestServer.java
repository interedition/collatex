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
