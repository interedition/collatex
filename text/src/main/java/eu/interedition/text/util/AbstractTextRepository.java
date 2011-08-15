package eu.interedition.text.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import eu.interedition.text.Text;
import eu.interedition.text.TextRepository;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractTextRepository implements TextRepository {

  protected TransformerFactory transformerFactory = TransformerFactory.newInstance();;

  public Text create(Source xml) throws IOException, TransformerException {
    final File xmlSource = File.createTempFile(getClass().getName(), ".xml");
    Reader xmlSourceReader = null;
    try {
      createTransformer().transform(xml, new StreamResult(xmlSource));

      final Text text = create(Text.Type.XML);
      xmlSourceReader = new InputStreamReader(new FileInputStream(xmlSource), Text.CHARSET);
      write(text, xmlSourceReader);
      return text;
    } finally {
      Closeables.close(xmlSourceReader, false);
      xmlSource.delete();
    }
  }

  @Override
  public void read(Text text, final Result xml) throws IOException, TransformerException {
    try {
      Preconditions.checkArgument(text.getType() == Text.Type.XML);
      read(text, new TextReader() {
        @Override
        public void read(Reader content, long contentLength) throws IOException {
          try {
            createTransformer().transform(new StreamSource(content), xml);
          } catch (TransformerException e) {
            throw Throwables.propagate(e);
          }
        }
      });
    } catch (IOException e) {
      throw e;
    } catch (Throwable t) {
      Throwables.propagateIfInstanceOf(Throwables.getRootCause(t), TransformerException.class);
      throw Throwables.propagate(t);
    }
  }

  protected Transformer createTransformer() throws TransformerException {
    final Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.ENCODING, Text.CHARSET.name());
    transformer.setOutputProperty(OutputKeys.INDENT, "no");
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    return transformer;
  }
}
