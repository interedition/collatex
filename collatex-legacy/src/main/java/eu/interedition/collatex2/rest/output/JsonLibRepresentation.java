package eu.interedition.collatex2.rest.output;

import java.io.IOException;
import java.io.Writer;

import net.sf.json.JSON;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.WriterRepresentation;

public class JsonLibRepresentation extends WriterRepresentation {

  private final JSON json;

  public JsonLibRepresentation(net.sf.json.JSON json) {
    super(MediaType.APPLICATION_JSON);
    setCharacterSet(CharacterSet.UTF_8);
    this.json = json;
  }

  @Override
  public void write(Writer writer) throws IOException {
    json.write(writer);
  }
}
