package eu.interedition.collatex2.rest;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import eu.interedition.collatex2.rest.resources.AlignmentResource;
import eu.interedition.collatex2.rest.resources.DarwinResource;
import eu.interedition.collatex2.rest.resources.ParserResource;
import eu.interedition.collatex2.rest.resources.UseCaseResource;

public class RestApplication extends Application {

  @Override
  public synchronized Restlet createRoot() {
    final Router router = new Router(getContext());
    router.attach("/usecases", UseCaseResource.class);
    router.attach("/usecases/{i}", UseCaseResource.class);
    router.attach("/darwin", DarwinResource.class);
    router.attach("/darwin/{i}", DarwinResource.class);
    router.attach("/alignment", AlignmentResource.class);
    router.attach("/jsoninput", ParserResource.class);
    return router;
  }

}
