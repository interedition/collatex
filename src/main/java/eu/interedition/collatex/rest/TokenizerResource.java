package eu.interedition.collatex.rest;

import java.util.Arrays;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class TokenizerResource extends ServerResource {

  private static final MediaType[] TYPES = { MediaType.TEXT_HTML, MediaType.TEXT_PLAIN };

  public TokenizerResource() {
    getVariants().put(Method.GET, Arrays.asList(TYPES));
  }

  @Override
  public Representation get(Variant variant) throws ResourceException {
    System.err.println("##" + getQuery().getNames());

    String witness = getQuery().getFirstValue("witness");

    System.err.println("!!" + witness);
    CharSequence text = witness;
    //CharSequence text = "Welkom bij de tokenizer";
    MediaType mediaType = MediaType.TEXT_PLAIN;
    StringRepresentation result = new StringRepresentation(text, mediaType);
    return result;
    //      try {
    //        String id = (String) getRequest().getAttributes().get("name");
    //        Database database = Database.getInstance();
    //        String name = database.getName(id);
    //        if (variant != null && variant.getMediaType() == MediaType.TEXT_PLAIN) {
    //          return new StringRepresentation(name, MediaType.TEXT_PLAIN);
    //        } else {
    //          StringBuilder builder = new StringBuilder();
    //          builder.append("<html><body>");
    //          builder.append("<h1>Author</h1>\n");
    //          builder.append("Name: ");
    //          builder.append(name);
    //          builder.append("</body></html>");
    //          return new StringRepresentation(builder.toString(), MediaType.TEXT_HTML);
    //        }
    //      } catch (SQLException e) {
    //        throw new ResourceException(404);
    //      }
  }

}
