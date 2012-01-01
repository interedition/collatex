package eu.interedition.web;

import com.google.common.collect.Maps;
import eu.interedition.collatex.graph.GraphFactory;
import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.TextRepository;
import eu.interedition.text.json.JSONSerializer;
import eu.interedition.text.xml.XMLSerializer;
import eu.interedition.web.collatex.GraphVizService;
import eu.interedition.web.io.CollationHttpMessageConverter;
import eu.interedition.web.io.JSONSerializationHttpMessageConverter;
import eu.interedition.web.io.VariantGraphJSONSerializer;
import eu.interedition.web.io.VariantGraphMLHttpMessageConverter;
import eu.interedition.web.io.VariantGraphTEIHttpMessageConverter;
import eu.interedition.web.io.VariantGraphVizHttpMessageConverter;
import eu.interedition.web.io.XMLSerializationHttpMessageConverter;
import eu.interedition.web.text.CrossOriginResourceSharingInterceptor;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
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
  private GraphFactory graphFactory;

  @Autowired
  private GraphVizService graphVizService;

  @Autowired
  private AnnotationRepository annotationRepository;
  
  @Autowired
  private TextRepository textRepository;

  @Autowired
  private XMLSerializer xmlSerializer;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new CrossOriginResourceSharingInterceptor());
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new CollationHttpMessageConverter(graphFactory));
    converters.add(new VariantGraphTEIHttpMessageConverter());
    converters.add(new VariantGraphMLHttpMessageConverter());
    converters.add(new VariantGraphVizHttpMessageConverter(graphVizService));
    converters.add(new VariantGraphJSONSerializer());
    converters.add(new JSONSerializationHttpMessageConverter(jsonSerializer(), objectMapper));
    converters.add(new XMLSerializationHttpMessageConverter(xmlSerializer));

    final MappingJacksonHttpMessageConverter defaultJsonConverter = new MappingJacksonHttpMessageConverter();
    defaultJsonConverter.setObjectMapper(objectMapper);
    converters.add(defaultJsonConverter);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/static/**").addResourceLocations("/WEB-INF/static/");
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/").setViewName("index");
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

  @Bean
  public MultipartResolver multipartResolver() {
    final CommonsMultipartResolver mr = new CommonsMultipartResolver();
    mr.setDefaultEncoding("UTF-8");
    mr.setMaxInMemorySize(102400);
    mr.setMaxUploadSize(20971520);
    return mr;
  }
  
  @Bean
  public JSONSerializer jsonSerializer() {
    final JSONSerializer js = new JSONSerializer();
    js.setAnnotationRepository(annotationRepository);
    js.setTextRepository(textRepository);
    return js;
  }
}
