package eu.interedition.collatex.simple;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex.Token;

public class SimpleWitnessTeiBuilderTest {

  @Ignore
  @Test
  public void testTei() throws IOException, XMLStreamException {
    InputStream resourceAsStream = getClass().getResourceAsStream("/matenadaran_1731.xml");
    //System.out.println(resourceAsStream.available());
    SimpleWitnessTeiBuilder builder = new SimpleWitnessTeiBuilder();
    SimpleWitness w = builder.read(resourceAsStream);
    for (Token t : w) {
      System.out.print(((SimpleToken)t).getContent()+" ");
    }
    System.out.println();
    InputStream stream1767 = getClass().getResourceAsStream("/matenadaran_1767.xml");
    //System.out.println(resourceAsStream.available());
    builder = new SimpleWitnessTeiBuilder();
    SimpleWitness w2 = builder.read(stream1767);
    for (Token t : w2) {
      System.out.print(((SimpleToken)t).getContent()+" ");
    }
  }
}
