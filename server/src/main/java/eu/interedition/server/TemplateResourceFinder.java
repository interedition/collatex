package eu.interedition.server;

import com.google.common.base.Throwables;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.io.IOException;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public class TemplateResourceFinder extends Finder {
  private final Configuration configuration;
  private final String templateName;

  public TemplateResourceFinder(Configuration configuration, String templateName) {
    this.configuration = configuration;
    this.templateName = templateName;
  }


  @Override
  public ServerResource find(Request request, Response response) {
    try {
      return new TemplateServerResource(configuration.getTemplate(templateName));
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  /**
  * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
  */
  public static class TemplateServerResource extends ServerResource {

    private final Template template;

    private TemplateServerResource(Template template) {
      super();
      this.template = template;
    }

    @Get("html")
    public Template getTemplate() {
      return template;
    }
  }
}
