package eu.interedition.collatex.cli;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleWitness;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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
          throws IOException, XPathExpressionException {
    InputStream stream = null;
    try {
      stream = url.openStream();
      if (tokenXPath != null) {
        final NodeList tokenNodes = (NodeList) tokenXPath.evaluate(new InputSource(stream), XPathConstants.NODESET);
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
    } finally {
      Closeables.close(stream, false);
    }
    return this;
  }
}
