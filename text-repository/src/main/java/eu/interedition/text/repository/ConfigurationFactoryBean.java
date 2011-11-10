package eu.interedition.text.repository;

import com.google.common.io.Closeables;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ConfigurationFactoryBean implements InitializingBean, FactoryBean<Properties> {

  private static final ClassPathResource CONFIG_RESOURCE = new ClassPathResource("config.properties", ConfigurationFactoryBean.class);

  private Properties properties;

  @Override
  public void afterPropertiesSet() throws Exception {
    properties = new Properties();
    InputStream configStream = null;
    try {
      properties.load(new InputStreamReader(configStream = CONFIG_RESOURCE.getInputStream(), "UTF-8"));
      for (Object key : properties.keySet()) {
        final String systemPropertyValue = System.getProperty((String) key);
        if (systemPropertyValue != null) {
          properties.setProperty((String) key, systemPropertyValue);
        }
      }
    } finally {
      Closeables.close(configStream, false);
    }
  }

  @Override
  public Properties getObject() throws Exception {
    return properties;
  }

  @Override
  public Class<?> getObjectType() {
    return Properties.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
