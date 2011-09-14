package eu.interedition.text.json;

import com.google.common.collect.BiMap;
import eu.interedition.text.*;
import eu.interedition.text.json.map.AnnotationSerializer;
import eu.interedition.text.json.map.QNameSerializer;
import eu.interedition.text.query.Operator;
import org.codehaus.jackson.JsonGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static eu.interedition.text.query.Criteria.and;
import static eu.interedition.text.query.Criteria.rangeOverlap;
import static eu.interedition.text.query.Criteria.text;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class JSONSerializer {

  public static final String TEXT_FIELD = "t";
  private static final String TEXT_LENGTH_FIELD = "l";
  public static final String ANNOTATIONS_FIELD = "a";

  public static final String ANNOTATION_DATA_FIELD = "d";
  private TextRepository textRepository;
  private AnnotationRepository annotationRepository;

  public void setTextRepository(TextRepository textRepository) {
    this.textRepository = textRepository;
  }

  public void setAnnotationRepository(AnnotationRepository annotationRepository) {
    this.annotationRepository = annotationRepository;
  }

  public void serialize(JsonGenerator jgen, Text text, final JSONSerializerConfiguration config) throws IOException {
    final Range range = config.getRange();

    jgen.writeStartObject();
    jgen.writeNumberField(TEXT_LENGTH_FIELD, text.getLength());
    jgen.writeStringField(TEXT_FIELD, textRepository.read(text, range == null ? new Range(0, text.getLength()) : range));

    final Operator criterion = and(config.getQuery(), text(text));
    if (range != null) {
      criterion.add(rangeOverlap(range));
    }
    final Map<Annotation,Map<QName,String>> annotations = annotationRepository.find(criterion, config.getDataSet());

    final BiMap<String, URI> nsMap = config.getNamespaceMappings();
    final BiMap<URI, String> prefixMap = (nsMap == null ? null : nsMap.inverse());

    if (!annotations.isEmpty()) {
      jgen.writeArrayFieldStart(ANNOTATIONS_FIELD);
      for (Map.Entry<Annotation, Map<QName, String>> a : annotations.entrySet()) {
        jgen.writeStartObject();

        final Annotation annotation = a.getKey();

        jgen.writeFieldName(AnnotationSerializer.NAME_FIELD);
        QNameSerializer.serialize(annotation.getName(), jgen, prefixMap);

        jgen.writeObjectField(AnnotationSerializer.RANGE_FIELD, annotation.getRange());

        final Map<QName, String> data = a.getValue();
        if (!data.isEmpty()) {
          jgen.writeArrayFieldStart(ANNOTATION_DATA_FIELD);
          for (Map.Entry<QName, String> dataEntry : data.entrySet()) {
            jgen.writeStartArray();
            QNameSerializer.serialize(dataEntry.getKey(), jgen, prefixMap);
            jgen.writeString(dataEntry.getValue());
            jgen.writeEndArray();
          }
          jgen.writeEndArray();
        }
        jgen.writeEndObject();
      }
      jgen.writeEndArray();
    }

    jgen.writeEndObject();
  }
}
