package eu.interedition.server.io;

import com.google.common.collect.Maps;
import freemarker.template.Configuration;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
public class CustomFreemarkerConfigurationFactoryBean extends AbstractFactoryBean<Configuration>  {

  @Autowired
  private Environment environment;

  @Override
  public Class<?> getObjectType() {
    return Configuration.class;
  }

  @Override
  protected Configuration createInstance() throws Exception {
    final Map<String,Object> shared = Maps.newHashMap();
    shared.put("cp", environment.getRequiredProperty("interedition.context_path"));

    final Properties settings = new Properties();
    settings.put("auto_include", "/header.ftl");
    settings.put("default_encoding", "UTF-8");
    settings.put("output_encoding", "UTF-8");
    settings.put("url_escaping_charset", "UTF-8");
    settings.put("strict_syntax", "true");
    settings.put("whitespace_stripping", "true");

    final FreeMarkerConfigurationFactory factory = new FreeMarkerConfigurationFactory();
    factory.setTemplateLoaderPath(environment.getRequiredProperty("interedition.templates"));
    factory.setFreemarkerSettings(settings);
    factory.setFreemarkerVariables(shared);
    return factory.createConfiguration();
  }
}
