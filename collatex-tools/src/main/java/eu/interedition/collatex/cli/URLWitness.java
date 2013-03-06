/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.cli;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class URLWitness extends SimpleWitness {

  public final URL url;

  public URLWitness(String sigil, URL url) {
    super(sigil);
    this.url = url;
  }

  public URLWitness read(
          Function<String, Iterable<String>> tokenizer,
          Function<String, String> normalizer,
          Charset charset,
          XPathExpression tokenXPath)
          throws IOException, XPathExpressionException, SAXException {
    InputStream stream = null;
    try {
      stream = url.openStream();
      if (tokenXPath != null) {
        final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document document = documentBuilder.parse(stream);
        document.normalizeDocument();

        final NodeList tokenNodes = (NodeList) tokenXPath.evaluate(document, XPathConstants.NODESET);
        final List<Token> tokens = Lists.newArrayListWithExpectedSize(tokenNodes.getLength());
        for (int nc = 0; nc < tokenNodes.getLength(); nc++) {
          final Node tokenNode = tokenNodes.item(nc);
          final String tokenText = tokenNode.getTextContent();
          tokens.add(new NodeToken(this, tokenText, normalizer.apply(tokenText), tokenNode));
        }
        setTokens(tokens);
      } else {
        final List<Token> tokens = Lists.newLinkedList();
        for (String tokenText : tokenizer.apply(CharStreams.toString(new InputStreamReader(stream, charset)))) {
          tokens.add(new SimpleToken(this, tokenText, normalizer.apply(tokenText)));
        }
        setTokens(tokens);
      }
    } catch (ParserConfigurationException e) {
      throw new SAXException(e);
    } finally {
      Closeables.close(stream, false);
    }
    return this;
  }
}
