package eu.interedition.server.io;

import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;

/**
 * Representation based on the Jackson library. It can serialize and deserialize
 * automatically in JSON.
 *
 * @see <a href="http://jackson.codehaus.org/">Jackson project</a>
 * @author Jerome Louvel
 */
public class JacksonRepresentation extends WriterRepresentation {

  /** The Jackson object mapper. */
  private final ObjectMapper objectMapper;

  /** The (parsed) object to format. */
  private final Object object;

  /** The object class to instantiate. */
  private final Class<?> objectClass;

  /** The JSON representation to parse. */
  private final Representation representation;

  public JacksonRepresentation(ObjectMapper objectMapper, Representation source, Class<?> target) {
    super(source.getMediaType());
    this.objectMapper = objectMapper;
    this.objectClass = target;
    this.object = null;
    this.representation = source;
  }

  public JacksonRepresentation(ObjectMapper objectMapper, MediaType mediaType, Object source, Class<?> target) {
    super(mediaType);
    this.objectMapper = objectMapper;
    this.objectClass = target;
    this.object = source;
    this.representation = null;
  }


  /**
   * Returns the wrapped object, deserializing the representation with Jackson
   * if necessary.
   *
   * @return The wrapped object.
   */
  public Object getObject() {
    if (object != null) {
      return object;
    }

    if (representation != null) {
      try {
        return objectMapper.readValue(representation.getStream(), this.objectClass);
      } catch (IOException e) {
        Context.getCurrentLogger().log(Level.WARNING, "Unable to parse the object with Jackson.", e);
      }
    }

    return null;
  }

  @Override
  public void write(Writer writer) throws IOException {
    if (representation != null) {
      representation.write(writer);
    } else if (object != null) {
      objectMapper.writeValue(writer, object);
    }
  }
}
