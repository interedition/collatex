package eu.interedition.text;

import java.io.IOException;
import java.io.Reader;
import java.util.SortedMap;
import java.util.SortedSet;

public interface TextRepository {

  Text create(Text.Type type);

  void delete(Text text);

  int length(Text text) throws IOException;

  void read(Text text, TextReader reader) throws IOException;

  String read(Text text, Range range) throws IOException;

  SortedMap<Range, String> bulkRead(Text text, SortedSet<Range> ranges) throws IOException;

  void write(Text text, Reader contents, int contentLength) throws IOException;

  interface TextReader {
    void read(Reader content, int contentLength) throws IOException;
  }
}
