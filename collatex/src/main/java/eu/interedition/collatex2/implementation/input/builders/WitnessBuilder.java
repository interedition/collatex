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

package eu.interedition.collatex2.implementation.input.builders;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import eu.interedition.collatex2.implementation.input.NormalizedToken;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.containers.witness.Witness;
import eu.interedition.collatex2.implementation.input.tokenization.WhitespaceTokenizer;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IToken;
import eu.interedition.collatex2.interfaces.ITokenNormalizer;
import eu.interedition.collatex2.interfaces.ITokenizer;
import eu.interedition.collatex2.interfaces.IWitness;

public class WitnessBuilder {

  private final ITokenNormalizer tokenNormalizer;

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

  public WitnessBuilder(ITokenNormalizer tokenNormalizer) {
    this.tokenNormalizer = tokenNormalizer;
  }

  public IWitness build(InputStream inputStream, ContentType contentType) throws SAXException, IOException {
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

  public IWitness build(String witnessId, String text, ITokenizer tokenizer) {
    Iterator<IToken> tokenIterator = tokenizer.tokenize(text).iterator();
    List<INormalizedToken> tokenList = Lists.newArrayList();
    while (tokenIterator.hasNext()) {
      IToken nextToken = tokenIterator.next();
      if (!nextToken.getContent().equals("")) {
        String normalized = tokenNormalizer.apply(nextToken).getNormalized();
        tokenList.add(new NormalizedToken(nextToken.getContent(), normalized));
      }
    }
    return new Witness(witnessId, tokenList);
  }

  public IWitness build(String witness) {
    /* no witnessId? generate a random one */
    return build(UUID.randomUUID().toString(), witness, new WhitespaceTokenizer());
  }

  public IWitness[] buildWitnesses(String... _witnesses) {
    /* no witnessId? generate a random one */
    IWitness[] witnesses = new IWitness[_witnesses.length];
    for (int i = 0; i < witnesses.length; i++) {
      witnesses[i] = build("" + (i + 1), _witnesses[i], new WhitespaceTokenizer());
    }
    return witnesses;
  }

}
