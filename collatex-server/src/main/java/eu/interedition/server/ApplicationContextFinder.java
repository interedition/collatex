package eu.interedition.server;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.springframework.context.support.AbstractApplicationContext;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public class ApplicationContextFinder<T extends ServerResource> extends Finder {
  private final AbstractApplicationContext applicationContext;
  private final Class<T> beanType;

  ApplicationContextFinder(Context context, AbstractApplicationContext applicationContext, Class<T> beanType) {
    super(context);
    this.applicationContext = applicationContext;
    this.beanType = beanType;
  }

  @Override
  public ServerResource find(Request request, Response response) {
    return applicationContext.getBean(beanType);
  }
}
