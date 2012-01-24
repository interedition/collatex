package eu.interedition.text;

import java.io.IOException;
import java.io.Reader;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public interface TextConsumer {
  void read(Reader content, long contentLength) throws IOException;
}
