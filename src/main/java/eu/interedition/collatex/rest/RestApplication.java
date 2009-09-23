package eu.interedition.collatex.rest;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class RestApplication extends Application {

  //    private final Database database;

  public RestApplication() {
  //      database = Database.getInstance();
  }

  //    public Database getDatabase() {
  //      return database;
  //    }

  @Override
  public synchronized Restlet createRoot() {
    Router router = new Router(getContext());
    router.attach("/tokenizer", TokenizerResource.class);
    router.attach("/alignment", AlignmentResource.class);
    //      router.attachDefault(ServiceResource.class);
    //      router.attach("/authors", AuthorsResource.class);
    //      router.attach("/author/{name}", AuthorResource.class);
    return router;
  }

}
