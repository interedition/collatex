package eu.interedition.collatex.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.collatex.web.io.CollationHttpMessageConverter;
import eu.interedition.collatex.web.io.VariantGraphJSONSerializer;
import eu.interedition.collatex.web.io.VariantGraphMLHttpMessageConverter;
import eu.interedition.collatex.web.io.VariantGraphTEIHttpMessageConverter;
import eu.interedition.collatex.web.io.VariantGraphVizHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@EnableWebMvc
@ComponentScan(basePackageClasses = WebConfiguration.class, includeFilters = { @ComponentScan.Filter(Controller.class) }, useDefaultFilters = false)
public class WebConfiguration extends WebMvcConfigurerAdapter {

  @Autowired
  private GraphVizService graphVizService;

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new CollationHttpMessageConverter());
    converters.add(new VariantGraphTEIHttpMessageConverter());
    converters.add(new VariantGraphMLHttpMessageConverter());
    converters.add(new VariantGraphVizHttpMessageConverter(graphVizService));
    converters.add(new VariantGraphJSONSerializer());
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/static/**").addResourceLocations("/WEB-INF/static/");
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/tutorial").setViewName("tutorial");
  }

  @Bean
  public FreeMarkerConfigurer freeMarkerConfigurer() {
    final Properties settings = new Properties();
    settings.put("auto_include", "/header.ftl");
    settings.put("default_encoding", "UTF-8");
    settings.put("output_encoding", "UTF-8");
    settings.put("url_escaping_charset", "UTF-8");
    settings.put("strict_syntax", "true");
    settings.put("whitespace_stripping", "true");

    final FreeMarkerConfigurer fc = new FreeMarkerConfigurer();
    fc.setTemplateLoaderPath("/WEB-INF/freemarker");
    fc.setFreemarkerSettings(settings);
    return fc;
  }

  @Bean
  public FreeMarkerViewResolver freeMarkerViewResolver() {
    final FreeMarkerViewResolver fmvr = new FreeMarkerViewResolver();
    fmvr.setOrder(Ordered.LOWEST_PRECEDENCE);
    fmvr.setContentType("text/html;charset=utf-8");
    fmvr.setPrefix("");
    fmvr.setSuffix(".ftl");
    return fmvr;
  }

  @Bean
  public ViewResolver viewResolver() {
    final HashMap<String,String> mediaTypes = Maps.newHashMap();
    mediaTypes.put("html", "text/html;charset=utf-8");
    mediaTypes.put("tei", "application/tei+xml");
    mediaTypes.put("graphml", "application/graphml+xml");
    mediaTypes.put("text", "text/plain");
    mediaTypes.put("svg", "image/svg+xml");
    mediaTypes.put("json", "application/json");

    final ContentNegotiatingViewResolver vr = new ContentNegotiatingViewResolver();
    vr.setDefaultContentType(new MediaType("text", "html", Charset.forName("UTF-8")));
    vr.setFavorParameter(true);
    vr.setParameterName("format");
    vr.setMediaTypes(mediaTypes);
    vr.setViewResolvers(Collections.<ViewResolver>singletonList(freeMarkerViewResolver()));
    return vr;
  }

  @Bean
  public LocaleResolver localeResolver() {
    return new AcceptHeaderLocaleResolver();
  }
}
