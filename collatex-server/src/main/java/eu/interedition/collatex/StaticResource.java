package eu.interedition.collatex;

import com.google.common.base.Strings;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.NotFoundException;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/static")
public class StaticResource {

  private static final MediaType TEXT_CSS_TYPE = new MediaType("text", "css");

  private final File staticPath;
  private final Date initial = new Date();

  @Inject
  public StaticResource(@Named("interedition.static") String staticPath) {
    this.staticPath = (Strings.isNullOrEmpty(staticPath) ? null : new File(staticPath));
  }

  @GET
  public Response rootNotFound() {
    throw new NotFoundException();
  }

  @Path("{path: .*}")
  @GET
  public Response stream(@Context Request request, @PathParam("path") String path) throws IOException {
    InputStream stream = null;
    Date lastModified = initial;
    if (staticPath == null) {
      stream = getClass().getResourceAsStream("/static/" + path);
    } else {
      final File file = new File(staticPath, path);
      if (file.isFile() && file.getCanonicalPath().startsWith(staticPath.getCanonicalPath())) {
        stream = new FileInputStream(file);
        lastModified = new Date(file.lastModified());
      }
    }

    if (stream == null) {
      throw new NotFoundException();
    }

    if (request.getMethod().equals("GET")) {
      final Response.ResponseBuilder preconditions = request.evaluatePreconditions(lastModified);
      if (preconditions != null) {
        Closeables.close(stream, false);
        throw new WebApplicationException(preconditions.build());
      }
    }

    final String extension = Files.getFileExtension(path);
    String contentType = "application/octet-stream";
    if ("js".equals(extension)) {
      contentType = "text/javascript";
    } else if ("css".equals(extension)) {
      contentType = "text/css";
    } else if ("jpg".equals(extension) || "jpeg".equals(extension)) {
      contentType = "image/jpeg";
    } else if ("gif".equals(extension)) {
      contentType = "image/gif";
    } else if ("png".equals(extension)) {
      contentType = "image/png";
    } else if ("ico".equals(extension)) {
      contentType = "image/vnd.microsoft.icon";
    }

    return Response.ok()
            .entity(stream)
            .lastModified(lastModified)
            .type(contentType)
            .build();
  }
}
