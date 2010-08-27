package eu.interedition.collatex2.graphvizrestlet;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class GraphvizRestApplication extends Application {
  @Override
  public synchronized Restlet createRoot() {
    final Router router = new Router(getContext());
    router.attach("/svg", SVGResource.class);
    return router;
  }

}
