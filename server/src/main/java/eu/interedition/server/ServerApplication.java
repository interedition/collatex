package eu.interedition.server;

import eu.interedition.server.collatex.VariantGraphResource;
import eu.interedition.server.ui.ServerConsole;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
public class ServerApplication extends Application implements InitializingBean {
  private static final String TX_ATTRIBUTE = "tx";

  @Autowired
  private AbstractApplicationContext applicationContext;

  @Autowired
  private PlatformTransactionManager transactionManager;

  public ServerApplication() {
    super(new Context());
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    final Router router = new Router(getContext());
    router.attachDefault(transactional(newFinder(VariantGraphResource.class)));
    setInboundRoot(router);
  }

  private Restlet transactional(Restlet restlet) {
    return new TransactionFilter(getContext(), restlet);
  }

  private <T extends ServerResource> ApplicationContextFinder<T> newFinder(Class<T> resourceType) {
    return new ApplicationContextFinder<T>(getContext(), resourceType);
  }

  /**
   * Entry point to the application.
   * <p/>
   * Upon start, the security manager is set to <code>null</code>, so Java Web Start's sandbox does not interfere with
   * the servlet container's classloading.
   *
   * @param args command line arguments (ignored)
   */
  public static void main(String... args) {
    System.setSecurityManager(null);

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

  private class ApplicationContextFinder<T extends ServerResource> extends Finder {
    private final Class<T> beanType;

    private ApplicationContextFinder(Context context, Class<T> beanType) {
      super(context);
      this.beanType = beanType;
    }

    @Override
    public ServerResource find(Request request, Response response) {
      return applicationContext.getBean(beanType);
    }
  }

  private class TransactionFilter extends Filter {

    public TransactionFilter(Context context, Restlet next) {
      super(context, next);
    }

    @Override
    protected int beforeHandle(Request request, Response response) {
      final Map<String, Object> responseAttributes = response.getAttributes();
      if (!responseAttributes.containsKey(TX_ATTRIBUTE)) {
        responseAttributes.put(TX_ATTRIBUTE, transactionManager.getTransaction(new DefaultTransactionDefinition()));
      }
      return super.beforeHandle(request, response);
    }

    @Override
    protected void afterHandle(Request request, Response response) {
      TransactionStatus tx = (TransactionStatus) response.getAttributes().remove(TX_ATTRIBUTE);
      if (tx != null) {
        if (response.getStatus().isError()) {
          tx.setRollbackOnly();
        }
        transactionManager.commit(tx);
      }
      super.afterHandle(request, response);
    }
  }
}
