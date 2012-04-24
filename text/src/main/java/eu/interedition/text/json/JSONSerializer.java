package eu.interedition.text.json;

import com.google.common.base.Throwables;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import eu.interedition.text.Annotation;
import eu.interedition.text.Name;
import eu.interedition.text.Text;
import eu.interedition.text.TextRange;
import eu.interedition.text.TextTarget;
import eu.interedition.text.query.AnnotationListenerAdapter;
import eu.interedition.text.json.map.AnnotationSerializer;
import eu.interedition.text.json.map.NameSerializer;
import eu.interedition.text.query.QueryOperator;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import static eu.interedition.text.query.QueryCriteria.and;
import static eu.interedition.text.query.QueryCriteria.rangeOverlap;
import static org.codehaus.jackson.JsonToken.*;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class JSONSerializer {

  public static final String TEXT_FIELD = "t";
  public static final String TEXT_LENGTH_FIELD = "l";
  public static final String ANNOTATIONS_FIELD = "a";
  public static final String ANNOTATION_DATA_FIELD = "d";
  public static final String NAMES_FIELD = "n";

  private SessionFactory sessionFactory;

  private int batchSize = 1024;

  @Required
  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public void serialize(final JsonGenerator jgen, Text text, final JSONSerializerConfiguration config) throws IOException {
    final TextTarget range = config.getRange();

    jgen.writeStartObject();

    final BiMap<String, URI> nsMap = HashBiMap.create(config.getNamespaceMappings());
    final BiMap<URI, String> prefixMap = (nsMap == null ? null : nsMap.inverse());

    final QueryOperator criterion = and(config.getQuery());
    if (range != null) {
      criterion.add(rangeOverlap(range));
    }

    final SortedSet<Name> names = Sets.newTreeSet();
    jgen.writeArrayFieldStart(ANNOTATIONS_FIELD);
    try {
      criterion.listen(sessionFactory.getCurrentSession(), text, new AnnotationListenerAdapter() {
        @Override
        public void start(long offset, Iterable<Annotation> annotations) {
          for (Annotation annotation : annotations) {
            try {
              jgen.writeStartObject();

              final Name an = annotation.getName();
              names.add(an);

              jgen.writeNumberField(AnnotationSerializer.NAME_FIELD, an.getId());

              final TextTarget ar = Iterables.getFirst(annotation.getTargets(), null);
              jgen.writeArrayFieldStart(AnnotationSerializer.RANGE_FIELD);
              jgen.writeNumber(ar.getStart());
              jgen.writeNumber(ar.getEnd());
              jgen.writeEndArray();


              jgen.writeFieldName(ANNOTATION_DATA_FIELD);
              jgen.writeTree(annotation.getData());

              jgen.writeEndObject();
            } catch (IOException e) {
              throw Throwables.propagate(e);
            }
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
      for (Name n : names) {
        jgen.writeFieldName(Long.toString(n.getId()));
        NameSerializer.serialize(n, jgen, prefixMap);
      }
      jgen.writeEndObject();
    }

    jgen.writeNumberField(TEXT_LENGTH_FIELD, text.getLength());
    jgen.writeStringField(TEXT_FIELD, CharStreams.toString(text.read(range == null ? new TextRange(0, text.getLength()) : range)));

    jgen.writeEndObject();
  }

  public void unserialize(final JsonParser jp, Text text) throws IOException {
    checkFormat(jp.nextToken().equals(START_ARRAY), "Expected start of array", jp);

    final Map<String, Name> names = Maps.newHashMap();
    checkFormat(jp.nextToken().equals(START_OBJECT), "Expected start of name hash", jp);
    while ((jp.nextToken() != null) && !END_OBJECT.equals(jp.getCurrentToken())) {
      checkFormat(jp.getCurrentToken().equals(FIELD_NAME), "Expected field name", jp);
      final String id = jp.getCurrentName();

      checkFormat(jp.nextToken() != null, "Expected QName", jp);
      names.put(id, jp.readValueAs(Name.class));
    }
    checkFormat(jp.getCurrentToken() != null, "Unexpected end of tokens", jp);

    final List<Annotation> batch = Lists.newArrayList();
    checkFormat(START_ARRAY.equals(jp.nextToken()), "Expected start of annotation array", jp);

    final Session session = sessionFactory.getCurrentSession();
    while ((jp.nextToken() != null) && !END_ARRAY.equals(jp.getCurrentToken())) {
      checkFormat(START_OBJECT.equals(jp.getCurrentToken()), "Expected start of annotation object", jp);

      final JsonNode annotationNode = jp.readValueAsTree();
      final String nameRef = annotationNode.path(AnnotationSerializer.NAME_FIELD).getTextValue();
      checkFormat(nameRef != null, "No name for annotation", jp);
      checkFormat(names.containsKey(nameRef), "Invalid name reference for annotation", jp);

      final Name name = names.get(nameRef);

      checkFormat(annotationNode.has(AnnotationSerializer.RANGE_FIELD), "No range for annotation", jp);

      final JsonParser rangeParser = annotationNode.get(AnnotationSerializer.RANGE_FIELD).traverse();
      rangeParser.setCodec(jp.getCodec());
      final TextTarget range = rangeParser.readValueAs(TextTarget.class);

      batch.add(new Annotation(name, range, annotationNode.get(ANNOTATION_DATA_FIELD)));
      if (batch.size() % batchSize == 0) {
        Annotation.create(session, batch);
        batch.clear();
      }
    }
    if (!batch.isEmpty()) {
      Annotation.create(session, batch);
    }

    checkFormat(jp.nextToken().equals(END_ARRAY), "Expected end of array", jp);
  }

  private static void checkFormat(boolean check, String message, JsonParser jp) throws JsonParseException {
    if (!check) {
      throw new JsonParseException(message, jp.getCurrentLocation());
    }
  }
}
