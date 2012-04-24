package eu.interedition.server.collatex;

import eu.interedition.collatex.graph.VariantGraph;
import org.restlet.data.MediaType;
import org.restlet.engine.Engine;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
public class VariantGraphConverter extends ConverterHelper implements InitializingBean {

  private static final VariantInfo VARIANT_SVG = new VariantInfo(MediaType.IMAGE_SVG);
  private static final VariantInfo VARIANT_TEXT = new VariantInfo(MediaType.TEXT_PLAIN);
  private static final VariantInfo VARIANT_GRAPHML = new VariantInfo(VariantGraphMLRepresentation.APPLICATION_GRAPHML);
  private static final VariantInfo VARIANT_TEI = new VariantInfo(VariantGraphTEIRepresentation.APPLICATION_TEI_XML);

  @Autowired
  AbstractApplicationContext applicationContext;

  String dotPath;

  public void setDotPath(File dotPath) {
    this.dotPath = (dotPath != null && dotPath.canExecute() ? dotPath.getPath() : null);
  }

  public boolean isSvgAvailable() {
    return (dotPath != null);
  }

  @Override
  public float score(Object source, Variant target, UniformResource resource) {
    float score = -1.0f;
    if (target == null) {
      score = 0.5f;
    } else if (target.isCompatible(VARIANT_TEXT) || target.isCompatible(VARIANT_GRAPHML) ||
            target.isCompatible(VARIANT_TEI) || (isSvgAvailable() && target.isCompatible(VARIANT_SVG))) {
      score = 1.0f;
    }
    return score;
  }

  @Override
  public <T> float score(Representation source, Class<T> target, UniformResource resource) {
    return -1.0f;
  }

  @Override
  public <T> T toObject(Representation source, Class<T> target, UniformResource resource) throws IOException {
    return null;
  }

  @Override
  public List<Class<?>> getObjectClasses(Variant source) {
    return null;
  }

  @Override
  public List<VariantInfo> getVariants(Class<?> source) {
    List<VariantInfo> variants = null;
    if (source != null && VariantGraph.class.isAssignableFrom(source)) {
      if (isSvgAvailable()) {
        variants = addVariant(variants, VARIANT_SVG);
      }
      variants = addVariant(variants, VARIANT_TEI);
      variants = addVariant(variants, VARIANT_GRAPHML);
      variants = addVariant(variants, VARIANT_TEXT);
    }
    return variants;
  }

  @Override
  public Representation toRepresentation(Object source, Variant target, UniformResource resource) throws IOException {
    final VariantGraph graph = (VariantGraph) source;
    if (target.isCompatible(VARIANT_TEXT)) {
      return applicationContext.getBean(VariantGraphvizDotRepresentation.class).forGraph(graph);
    } else if (target.isCompatible(VARIANT_GRAPHML)) {
      return applicationContext.getBean(VariantGraphMLRepresentation.class).forGraph(graph);
    } else if (target.isCompatible(VARIANT_TEI)) {
      return applicationContext.getBean(VariantGraphTEIRepresentation.class).forGraph(graph);
    } else if (isSvgAvailable() && target.isCompatible(VARIANT_SVG)) {
      return applicationContext.getBean(VariantGraphSVGRepresentation.class).forGraph(graph);
    }
    return null;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Engine.getInstance().getRegisteredConverters().add(this);
  }

}
