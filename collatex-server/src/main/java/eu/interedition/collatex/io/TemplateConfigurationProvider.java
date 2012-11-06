package eu.interedition.collatex.io;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.name.Named;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TemplateConfigurationProvider implements Provider<Configuration> {

  private final String contextPath;
  private final String templatePath;

  @Inject
  public TemplateConfigurationProvider(@Named("interedition.context_path") String contextPath,
                                       @Named("interedition.templates") String templatePath) {
    this.contextPath = contextPath;
    this.templatePath = templatePath;
  }

  @Override
  public Configuration get() {
    try {
      final Configuration configuration = new Configuration();
      configuration.setSharedVariable("cp", contextPath);
      configuration.setAutoIncludes(Collections.singletonList("/header.ftl"));
      configuration.setDefaultEncoding("UTF-8");
      configuration.setOutputEncoding("UTF-8");
      configuration.setURLEscapingCharset("UTF-8");
      configuration.setStrictSyntaxMode(true);
      configuration.setWhitespaceStripping(true);
      configuration.setTemplateLoader(
              Strings.isNullOrEmpty(templatePath)
              ? new ClassTemplateLoader(getClass(), "/templates")
              : new FileTemplateLoader(new File(templatePath))
      );
      return configuration;
    } catch (TemplateModelException e) {
      throw new ProvisionException("Freemarker error while creating template configuration", e);
    } catch (IOException e) {
      throw new ProvisionException("I/O error while creating template configuration", e);
    }
  }
}
