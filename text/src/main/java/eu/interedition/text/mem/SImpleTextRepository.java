package eu.interedition.text.mem;

import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import eu.interedition.text.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleTextRepository implements TextRepository {
  public Text create(Text.Type type) {
    return new SimpleText(type);
  }

  public void delete(Text text) {
    for (Annotation a : ((SimpleText)text)) {
      for (AnnotationLink l : ((SimpleAnnotation)a).getLinks()) {
        ((SimpleAnnotationLink)l).remove(a);
      }
    }
  }

  public int length(Text text) throws IOException {
    return ((SimpleText) text).getContent().length();
  }

  public void read(Text text, TextReader reader) throws IOException {
    final String content = ((SimpleText) text).getContent();
    reader.read(new StringReader(content), content.length());
  }

  public String read(Text text, Range range) throws IOException {
    return ((SimpleText) text).getContent().substring(range.getStart(), range.getEnd());
  }

  public SortedMap<Range, String> bulkRead(Text text, SortedSet<Range> ranges) throws IOException {
    final SortedMap<Range, String> read = Maps.newTreeMap();
    for (Range r : ranges) {
      read.put(r, read(text, r));
    }
    return read;
  }

  public void write(Text text, Reader contents, int contentLength) throws IOException {
    ((SimpleText)text).setContent(CharStreams.toString(contents));
  }
}
