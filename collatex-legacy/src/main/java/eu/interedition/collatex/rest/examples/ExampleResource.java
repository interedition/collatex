package eu.interedition.collatex.rest.examples;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class ExampleResource extends ServerResource {

  public ExampleResource() {
    getVariants().add(new Variant(MediaType.TEXT_HTML));
    getVariants().add(new Variant(MediaType.TEXT_PLAIN));
  }

  @Override
  public Representation get(Variant variant) throws ResourceException {
    return null;
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
