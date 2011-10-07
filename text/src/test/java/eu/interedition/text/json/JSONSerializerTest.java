package eu.interedition.text.json;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import eu.interedition.text.*;
import eu.interedition.text.json.map.TextSerializerModule;
import eu.interedition.text.query.Criteria;
import eu.interedition.text.query.Criterion;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class JSONSerializerTest extends AbstractTestResourceTest {

  @Autowired
  private JSONSerializer js;

  @Test
  public void simpleSerialization() throws IOException {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new TextSerializerModule());

    final JsonGenerator jgen = objectMapper.getJsonFactory().createJsonGenerator(System.out, JsonEncoding.UTF8);
    jgen.useDefaultPrettyPrinter();

    js.serialize(jgen, text(), new JSONSerializerConfiguration() {
      @Override
      public Range getRange() {
        return null;
      }

      @Override
      public Map<String, URI> getNamespaceMappings() {
        Map<String, URI> nsMap = Maps.newHashMap();
        nsMap.put("tei", TextConstants.TEI_NS);
        nsMap.put("xml", TextConstants.XML_NS_URI);
        return nsMap;
      }

      @Override
      public Set<QName> getDataSet() {
        return null;
      }

      @Override
      public Criterion getQuery() {
        return Criteria.any();
      }
    });
  }

}
