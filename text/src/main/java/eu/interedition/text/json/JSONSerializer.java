package eu.interedition.text.json;

import com.google.common.base.Throwables;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import eu.interedition.text.*;
import eu.interedition.text.json.map.AnnotationSerializer;
import eu.interedition.text.json.map.QNameSerializer;
import eu.interedition.text.query.Operator;
import eu.interedition.text.rdbms.RelationalQName;
import org.codehaus.jackson.JsonGenerator;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.SortedSet;

import static com.google.common.base.Preconditions.checkState;
import static eu.interedition.text.query.Criteria.*;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class JSONSerializer {

  public static final String TEXT_FIELD = "t";
  public static final String TEXT_LENGTH_FIELD = "l";
  public static final String ANNOTATIONS_FIELD = "a";
  public static final String ANNOTATION_DATA_FIELD = "d";
  public static final String NAMES_FIELD = "n";


  private TextRepository textRepository;
  private AnnotationRepository annotationRepository;

  @Required
  public void setTextRepository(TextRepository textRepository) {
    this.textRepository = textRepository;
  }

  @Required
  public void setAnnotationRepository(AnnotationRepository annotationRepository) {
    this.annotationRepository = annotationRepository;
  }

  public void serialize(final JsonGenerator jgen, Text text, final JSONSerializerConfiguration config) throws IOException {
    final Range range = config.getRange();

    jgen.writeStartObject();

    final BiMap<String, URI> nsMap = HashBiMap.create(config.getNamespaceMappings());
    final BiMap<URI, String> prefixMap = (nsMap == null ? null : nsMap.inverse());

    final Operator criterion = and(config.getQuery(), text(text));
    if (range != null) {
      criterion.add(rangeOverlap(range));
    }

    final SortedSet<RelationalQName> names = Sets.newTreeSet();
    jgen.writeArrayFieldStart(ANNOTATIONS_FIELD);
    try {
      annotationRepository.scroll(criterion, config.getDataSet(), new AnnotationRepository.AnnotationCallback() {
        @Override
        public void annotation(Annotation annotation, Map<QName, String> data) {
          try {
            jgen.writeStartObject();

            final QName an = annotation.getName();
            checkState(RelationalQName.class.isAssignableFrom(an.getClass()), an.getClass().toString());
            names.add((RelationalQName) an);

            jgen.writeNumberField(AnnotationSerializer.NAME_FIELD, ((RelationalQName) an).getId());

            final Range ar = annotation.getRange();
            jgen.writeArrayFieldStart(AnnotationSerializer.RANGE_FIELD);
            jgen.writeNumber(ar.getStart());
            jgen.writeNumber(ar.getEnd());
            jgen.writeEndArray();


            if (!data.isEmpty()) {
              jgen.writeArrayFieldStart(ANNOTATION_DATA_FIELD);
              for (Map.Entry<QName, String> dataEntry : data.entrySet()) {
                final QName dn = dataEntry.getKey();
                checkState(RelationalQName.class.isAssignableFrom(dn.getClass()), dn.getClass().toString());
                names.add((RelationalQName) dn);

                jgen.writeStartArray();
                jgen.writeNumber(((RelationalQName) dn).getId());
                jgen.writeString(dataEntry.getValue());
                jgen.writeEndArray();
              }
              jgen.writeEndArray();
            }
            jgen.writeEndObject();
          } catch (IOException e) {
            throw Throwables.propagate(e);
          }
        }
      });
    } catch (Throwable t) {
      Throwables.propagateIfInstanceOf(t, IOException.class);
      Throwables.propagateIfInstanceOf(Throwables.getRootCause(t), IOException.class);
      throw Throwables.propagate(t);
    }
    jgen.writeEndArray();

    if (!names.isEmpty()) {
      jgen.writeObjectFieldStart(NAMES_FIELD);
      for (RelationalQName n : names) {
        jgen.writeFieldName(Long.toString(n.getId()));
        QNameSerializer.serialize(n, jgen, prefixMap);
      }
      jgen.writeEndObject();
    }

    jgen.writeNumberField(TEXT_LENGTH_FIELD, text.getLength());
    jgen.writeStringField(TEXT_FIELD, textRepository.read(text, range == null ? new Range(0, text.getLength()) : range));

    jgen.writeEndObject();
  }
}
