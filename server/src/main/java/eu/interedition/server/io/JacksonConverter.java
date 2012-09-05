package eu.interedition.server.io;

import eu.interedition.server.collatex.CollateXModule;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.Engine;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Converter between the JSON and Representation classes based on Jackson.
 *
 * @author Jerome Louvel
 */
@Component
public class JacksonConverter extends ConverterHelper implements InitializingBean {

  private static final VariantInfo VARIANT_JSON = new VariantInfo(MediaType.APPLICATION_JSON);

  @Autowired
  private CollateXModule collateXModule;

  private ObjectMapper objectMapper;

  @Override
  public void afterPropertiesSet() throws Exception {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.getJsonFactory().configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
    this.objectMapper.registerModule(collateXModule);

    Engine.getInstance().getRegisteredConverters().add(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T toObject(Representation source, Class<T> target, UniformResource resource) throws IOException {
    Object result = null;

    // The source for the Jackson conversion
    JacksonRepresentation repr = null;

    if (source instanceof JacksonRepresentation) {
      repr = (JacksonRepresentation) source;
    } else if (VARIANT_JSON.isCompatible(source)) {
      repr = new JacksonRepresentation(objectMapper, source, target);
    }

    if (repr != null) {
      // Handle the conversion
      if ((target != null) && JacksonRepresentation.class.isAssignableFrom(target)) {
        result = repr;
      } else {
        result = repr.getObject();
      }
    }

    return (T) result;
  }

  @Override
  public Representation toRepresentation(Object source, Variant target, UniformResource resource) {
    if (source instanceof JacksonRepresentation) {
      return (JacksonRepresentation) source;
    }

    if (target.getMediaType() == null) {
      target.setMediaType(MediaType.APPLICATION_JSON);
    }

    if (VARIANT_JSON.isCompatible(target)) {
      return new JacksonRepresentation(objectMapper, target.getMediaType(), source, (source == null) ? null : source.getClass());
    }

    return null;
  }

  @Override
  public List<Class<?>> getObjectClasses(Variant source) {
    List<Class<?>> result = null;

    if (VARIANT_JSON.isCompatible(source)) {
      result = addObjectClass(result, Object.class);
      result = addObjectClass(result, JacksonRepresentation.class);
    }

    return result;
  }

  @Override
  public List<VariantInfo> getVariants(Class<?> source) {
    List<VariantInfo> result = null;

    if (source != null) {
      result = addVariant(result, VARIANT_JSON);
    }

    return result;
  }

  @Override
  public float score(Object source, Variant target, UniformResource resource) {
    float result = -1.0F;

    if (source instanceof JacksonRepresentation) {
      result = 1.0F;
    } else {
      if (target == null) {
        result = 0.5F;
      } else if (VARIANT_JSON.isCompatible(target)) {
        result = 0.8F;
      } else {
        result = 0.5F;
      }
    }

    return result;
  }

  @Override
  public <T> float score(Representation source, Class<T> target, UniformResource resource) {
    float result = -1.0F;

    if (source instanceof JacksonRepresentation) {
      result = 1.0F;
    } else if ((target != null) && JacksonRepresentation.class.isAssignableFrom(target)) {
      result = 1.0F;
    } else if (VARIANT_JSON.isCompatible(source)) {
      result = 0.8F;
    }

    return result;
  }

  @Override
  public <T> void updatePreferences(List<Preference<MediaType>> preferences, Class<T> entity) {
    updatePreferences(preferences, MediaType.APPLICATION_JSON, 1.0F);
  }
}
