package eu.interedition.collatex.input.builders;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import eu.interedition.collatex.Util;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.tokenization.WitnessTokenizer;

public class WitnessBuilder {

  public enum ContentType {
    TEXT_XML("text/xml"), TEXT_PLAIN("text/plain");

    private String type;

    private ContentType(String type) {
      this.type = type;
    }

    public static ContentType value(String contentType) {
      for (ContentType cType : ContentType.values()) {
        if (contentType.equals(cType.type)) return cType;
      }
      return null;
    }
  }

  public WitnessBuilder() {

  }

  public Segment build(InputStream inputStream, ContentType contentType) throws SAXException, IOException {
    if (contentType == null) throw new IllegalArgumentException("Given content type is unsupported!");
    switch (contentType) {
    case TEXT_PLAIN:
      return new WitnessPlainBuilder().build(inputStream);
    case TEXT_XML:
      return new WitnessXmlBuilder().build(inputStream);
    default:
      throw new IllegalArgumentException("Given content type is unsupported!");
    }
  }

  public Segment build(String witnessId, String witness) {
    WitnessTokenizer tokenizer = new WitnessTokenizer(witness, true);
    List<Word> words = Lists.newArrayList();
    int position = 1;
    while (tokenizer.hasNextToken()) {
      words.add(new Word(witnessId, tokenizer.nextToken(), position));
      position++;
    }
    return new Segment(words.toArray(new Word[0]));
  }

  public Segment build(String witness) {
    /* no witnessId? generate a random one */
    return build(Util.generateRandomId(), witness);
  }

  public Segment[] buildWitnesses(String... _witnesses) {
    /* no witnessId? generate a random one */
    Segment[] witnesses = new Segment[_witnesses.length];
    for (int i = 0; i < witnesses.length; i++) {
      witnesses[i] = build("" + (i + 1), _witnesses[i]);
    }
    return witnesses;
  }

}
