package eu.interedition.collatex.http;

import com.google.common.io.Closeables;
import com.sun.jersey.server.impl.model.HttpHelper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Provider
@Produces({"text/html", "*/*"})
public class TemplateMessageBodyWriter implements MessageBodyWriter<Template> {
  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return Template.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(Template template, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(Template template, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    Writer writer = null;
    try {
      template.process(null, writer = new OutputStreamWriter(entityStream, "UTF-8"));
    } catch (TemplateException e) {
      throw new WebApplicationException(e);
    } finally {
      Closeables.close(writer, false);
    }
  }
}
