package eu.interedition.web;

import eu.interedition.text.json.map.TextSerializerModule;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Service;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Service("objectMapper")
public class ObjectMapperFactoryBean extends AbstractFactoryBean<ObjectMapper> {

  @Override
  public Class<?> getObjectType() {
    return ObjectMapper.class;
  }

  @Override
  protected ObjectMapper createInstance() throws Exception {
    final ObjectMapper om = new ObjectMapper();
    om.registerModule(new TextSerializerModule());
    return om;
  }
}
