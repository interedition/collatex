/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.input;

import eu.interedition.collatex.TokenNormalizer;
import eu.interedition.collatex.Tokenizer;
import eu.interedition.collatex.Witness;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class WitnessBuilder {

  private final TokenNormalizer tokenNormalizer;

  public enum ContentType {
    TEXT_XML("text/xml"), TEXT_PLAIN("text/plain");

    private String type;

    private ContentType(String type) {
      this.type = type;
    }

    public static ContentType value(String contentType) {
      for (ContentType cType : ContentType.values()) {
        if (contentType.equals(cType.type)) {
          return cType;
        }
      }
      return null;
    }
  }

  public WitnessBuilder(TokenNormalizer tokenNormalizer) {
    this.tokenNormalizer = tokenNormalizer;
  }

  public Witness build(InputStream inputStream, ContentType contentType) throws SAXException, IOException {
    if (contentType == null) {
      throw new IllegalArgumentException("Given content type is unsupported!");
    }
    switch (contentType) {
      case TEXT_PLAIN:
        return new WitnessPlainBuilder(tokenNormalizer).build(inputStream);
      case TEXT_XML:
        return new WitnessTeiBuilder(tokenNormalizer).build(inputStream);
      default:
        throw new IllegalArgumentException("Given content type is unsupported!");
    }
  }

  public Witness build(String witnessId, String text, Tokenizer tokenizer) {
    final SimpleWitness witness = new SimpleWitness(witnessId);
    witness.setTokens(tokenizer.tokenize(witness, text));
    return witness;
  }

  public Witness build(String witness) {
    /* no witnessId? generate a random one */
    return build(UUID.randomUUID().toString(), witness, new WhitespaceTokenizer());
  }

  public Witness[] buildWitnesses(String... _witnesses) {
    /* no witnessId? generate a random one */
    Witness[] witnesses = new Witness[_witnesses.length];
    for (int i = 0; i < witnesses.length; i++) {
      witnesses[i] = build("" + (i + 1), _witnesses[i], new WhitespaceTokenizer());
    }
    return witnesses;
  }

}
