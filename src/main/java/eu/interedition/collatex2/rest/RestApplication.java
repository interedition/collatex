package eu.interedition.collatex2.rest;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class RestApplication extends Application {

  @Override
  public synchronized Restlet createRoot() {
    final Router router = new Router(getContext());
    router.attach("/usecases", UseCaseResource.class);
    router.attach("/usecases/{i}", UseCaseResource.class);
    router.attach("/darwin", DarwinResource.class);
    return router;
  }

}
