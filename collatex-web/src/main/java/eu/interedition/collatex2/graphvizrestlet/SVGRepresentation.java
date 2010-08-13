package eu.interedition.collatex2.graphvizrestlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.restlet.data.MediaType;
import org.restlet.representation.WriterRepresentation;

public class SVGRepresentation extends WriterRepresentation {
  private final InputStream inputStream;

  public SVGRepresentation(InputStream inputStream) {
    super(MediaType.IMAGE_SVG);
    this.inputStream = inputStream;
  }

  @Override
  public void write(Writer writer) throws IOException {
    IOUtils.copy(inputStream, writer);
  }

}
