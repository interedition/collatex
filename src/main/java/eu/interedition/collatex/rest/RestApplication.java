package eu.interedition.collatex.rest;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class RestApplication extends Application {

  @Override
  public synchronized Restlet createRoot() {
    final Router router = new Router(getContext());
    router.attach("/tokenizer", TokenizerResource.class);
    router.attach("/alignment", AlignmentResource.class);
    router.attach("/segmentation", SegmentationResource.class);
    router.attach("/jsoninput", ParserResource.class);
    router.attach("/demo", DemoResource.class);
    router.attach("/beckett", BeckettResource.class);
    return router;
  }

}
