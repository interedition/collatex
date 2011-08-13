package eu.interedition.text.util;

import com.google.common.io.Closeables;
import eu.interedition.text.Text;
import eu.interedition.text.TextRepository;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
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

      final Transformer serializer = transformerFactory.newTransformer();
      serializer.setOutputProperty(OutputKeys.METHOD, "xml");
      serializer.setOutputProperty(OutputKeys.ENCODING, Text.CHARSET.name());
      serializer.setOutputProperty(OutputKeys.INDENT, "no");
      serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      serializer.transform(xml, new StreamResult(xmlSource));

      final Text text = create(Text.Type.XML);
      xmlSourceReader = new InputStreamReader(new FileInputStream(xmlSource), Text.CHARSET);
      write(text, xmlSourceReader);
      return text;
    } finally {
      Closeables.close(xmlSourceReader, false);
      xmlSource.delete();
    }
  }
}
