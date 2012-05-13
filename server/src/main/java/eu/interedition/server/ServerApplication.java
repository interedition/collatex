package eu.interedition.server;

import eu.interedition.server.collatex.VariantGraphResource;
import eu.interedition.server.io.ComboResourceFinder;
import eu.interedition.server.ui.ServerConsole;
import freemarker.template.Configuration;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.CharacterSet;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.routing.VirtualHost;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import static org.restlet.routing.Template.MODE_EQUALS;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
public class ServerApplication extends Application implements InitializingBean {
  @Autowired
  private AbstractApplicationContext applicationContext;

  @Autowired
  private Environment environment;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private Configuration freemarkerConfiguration;

  @Autowired
  private ComboResourceFinder comboResourceFinder;

  public ServerApplication() {
    super();
  }

  @Override
  public Restlet createInboundRoot() {
    final Context context = getContext();

    final Router router = new Router(context);
    router.attach("/", new TemplateResourceFinder(freemarkerConfiguration, "index.ftl"), MODE_EQUALS);
    router.attach("/collate", transactional(newFinder(VariantGraphResource.class)), MODE_EQUALS);
    router.attach("/collate/apidocs", new TemplateResourceFinder(freemarkerConfiguration, "collate/apidocs.ftl"), MODE_EQUALS);
    router.attach("/collate/console", new TemplateResourceFinder(freemarkerConfiguration, "collate/console.ftl"), MODE_EQUALS);
    router.attach("/collate/darwin", new TemplateResourceFinder(freemarkerConfiguration, "collate/darwin-example.ftl"), MODE_EQUALS);
    router.attach("/resources", comboResourceFinder);
    router.attach("/static/interedition", new Directory(context.createChildContext(), "clap://class/eu/interedition/style"));
    router.attach("/static", new Directory(context.createChildContext(), environment.getRequiredProperty("interedition.static")));
    return router;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    getMetadataService().setDefaultCharacterSet(CharacterSet.UTF_8);
  }

  private Restlet transactional(Restlet restlet) {
    return new TransactionFilter(getContext().createChildContext(), restlet, transactionManager);
  }

  private <T extends ServerResource> ApplicationContextFinder<T> newFinder(Class<T> resourceType) {
    return new ApplicationContextFinder<T>(getContext().createChildContext(), applicationContext, resourceType);
  }

  /**
   * Entry point to the application.
   * <p/>
   * Upon start, the security manager is set to <code>null</code>, so Java Web Start's sandbox does not interfere with
   * classloading.
   *
   * @param args command line arguments (ignored)
   */
  public static void main(String... args) {
    System.setSecurityManager(null);

    final Logger logger = Logger.getLogger("");
    for (Handler handler : logger.getHandlers()) {
      if (ConsoleHandler.class.isAssignableFrom(handler.getClass())) {
        logger.removeHandler(handler);
      }
    }
    SLF4JBridgeHandler.install();

    try {
      final ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"/application-context.xml"}, false);

      final Properties config = new Properties();
      config.put("interedition.data", dataDirectory());

      final MutablePropertySources envProps = ctx.getEnvironment().getPropertySources();
      envProps.addLast(new PropertiesPropertySource("system", System.getProperties()));
      envProps.addLast(new PropertiesPropertySource("detected", config));
      envProps.addLast(new ResourcePropertySource(new ClassPathResource("/config.properties", ServerApplication.class)));

      ctx.registerShutdownHook();
      ctx.refresh();
      ctx.getBean(ServerConsole.class).setVisible(true);
    } catch (IOException e) {
      System.exit(1);
    }
  }


  /**
   * Platform-dependent setup of the filesystem location where this application stores its data.
   *
   * @return the data directory file
   * @throws IOException in case the data directory cannot be created or accessed
   */
  protected static File dataDirectory() throws IOException {
    final File userHome = new File(System.getProperty("user.home"));
    final String osName = System.getProperty("os.name").toLowerCase();

    File dataDirectory;
    if (osName.contains("mac os x")) {
      dataDirectory = new File(userHome, "Library/Application Support/Interedition");
    } else if (osName.contains("windows")) {
      dataDirectory = new File(userHome, "Application Data/Interedition");
    } else {
      dataDirectory = new File(userHome, ".interedition");
    }

    if (!dataDirectory.isDirectory() && !dataDirectory.mkdirs()) {
      throw ServerConsole.error(null, new IOException("Cannot create data directory " + dataDirectory.getPath()), null);
    }

    try {
      dataDirectory = dataDirectory.getCanonicalFile();
    } catch (IOException e) {
      throw ServerConsole.error(null, e, "Cannot determine canonical path of " + dataDirectory.getPath());
    }

    return dataDirectory;
  }

}
