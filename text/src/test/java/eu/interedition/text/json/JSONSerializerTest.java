package eu.interedition.text.json;

import com.google.common.collect.Maps;
import com.google.common.io.NullOutputStream;
import eu.interedition.text.*;
import eu.interedition.text.json.map.TextSerializerModule;
import eu.interedition.text.query.Criteria;
import eu.interedition.text.query.Criterion;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
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

    final StringWriter json = new StringWriter();
    final JsonFactory jf = objectMapper.getJsonFactory();
    final JsonGenerator jg;
    if (LOG.isDebugEnabled()) {
      jg = jf.createJsonGenerator(json);
      jg.useDefaultPrettyPrinter();
    } else {
      jg = jf.createJsonGenerator(new NullOutputStream());
    }

    js.serialize(jg, text(), new JSONSerializerConfiguration() {
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
      public Set<Name> getDataSet() {
        return null;
      }

      @Override
      public Criterion getQuery() {
        return Criteria.any();
      }
    });
    if (LOG.isDebugEnabled()) {
      LOG.debug(json.toString());
    }
  }

}
