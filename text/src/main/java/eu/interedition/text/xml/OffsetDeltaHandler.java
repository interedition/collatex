package eu.interedition.text.xml;

import eu.interedition.text.Range;
import eu.interedition.text.Text;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface OffsetDeltaHandler {

  void newOffsetDelta(Text text, Text source, Range textRange, Range sourceRange);
}
