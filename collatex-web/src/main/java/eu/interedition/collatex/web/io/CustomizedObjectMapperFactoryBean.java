package eu.interedition.collatex.web.io;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Service;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Service("objectMapper")
public class CustomizedObjectMapperFactoryBean extends AbstractFactoryBean<ObjectMapper> {
  @Override
  public Class<?> getObjectType() {
    return ObjectMapper.class;
  }

  @Override
  protected ObjectMapper createInstance() throws Exception {
    final SimpleModule module = new SimpleModule(getClass().getPackage().getName(), new Version(1, 0, 0, null));
    module.addSerializer(new AlignmentTableSerializer());
    module.addSerializer(new VariantGraphSerializer());

    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(module);
    return objectMapper;
  }

}