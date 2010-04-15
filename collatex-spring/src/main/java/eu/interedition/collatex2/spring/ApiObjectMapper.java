package eu.interedition.collatex2.spring;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import eu.interedition.collatex2.interfaces.IAlignmentTable;

@Service
public class ApiObjectMapper extends ObjectMapper implements InitializingBean {

  @Override
  public void afterPropertiesSet() throws Exception {
    CustomSerializerFactory f = new CustomSerializerFactory();
    f.addGenericMapping(IAlignmentTable.class, new ApiAlignmentTableSerializer());
    
    setSerializerFactory(f);
    
  }
}
