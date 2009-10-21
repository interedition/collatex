package eu.interedition.collatex.input.builders;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.SAXException;

import eu.interedition.collatex.input.Segment;

public abstract class WitnessStreamBuilder extends WitnessBuilder {

  public abstract Segment build(InputStream inputStream) throws SAXException, IOException;

}
