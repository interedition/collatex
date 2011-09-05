package eu.interedition.text.graph;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.util.AbstractTextRepository;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.*;

import static eu.interedition.text.graph.TextRelationshipType.HAS_TEXT;
import static java.nio.channels.FileChannel.MapMode.READ_ONLY;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class GraphTextRepository extends AbstractTextRepository {
  public static final Charset CHARSET = Charset.forName("UTF-16BE");

  private GraphDatabaseService db;
  private File contentStore;

  @Required
  public void setGraphDataSource(GraphDataSource ds) {
    this.db = ds.getGraphDatabaseService();
    this.contentStore = ds.getContentStore();
  }

  @Override
  public Text create(Text.Type type) {
    final String uuid = UUID.randomUUID().toString();

    final Node node = db.createNode();

    node.setProperty(GraphText.PROP_TYPE, type.ordinal());
    node.setProperty(GraphText.PROP_LENGTH, 0);
    node.setProperty(GraphText.PROP_DIGEST, NULL_CONTENT_DIGEST);
    node.setProperty(GraphText.PROP_UUID, uuid);

    db.getReferenceNode().createRelationshipTo(node, HAS_TEXT);

    try {
      Files.write("", new File(contentStore, uuid), Text.CHARSET);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
    return new GraphText(node);
  }

  @Override
  public void delete(Text text) {
    GraphText graphText = (GraphText) text;

    final File contentFile = new File(contentStore, graphText.getUUID());
    Preconditions.checkState(contentFile.delete(), contentFile + " could not be deleted");

    graphText.getNode().delete();
  }

  @Override
  public void read(Text text, TextReader reader) throws IOException {
    final GraphText graphText = (GraphText) text;
    final Reader fileReader = Files.newReader(new File(contentStore, graphText.getUUID()), Text.CHARSET);
    try {
      reader.read(fileReader, graphText.getLength());
    } finally {
      Closeables.close(fileReader, false);
    }
  }

  @Override
  public void read(Text text, Range range, TextReader reader) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public SortedMap<Range, String> bulkRead(Text text, SortedSet<Range> ranges) throws IOException {

    final MappedByteBuffer contents = Files.map(new File(contentStore, ((GraphText) text).getUUID()), READ_ONLY);
    final CharBuffer charContents = contents.asCharBuffer();

    final SortedMap<Range, String> result = Maps.newTreeMap();
    for (Range r : ranges) {
      Preconditions.checkArgument(r.getEnd() <= charContents.length());

      final char[] rangeContent = new char[(int) r.length()];
      charContents.position((int) r.getStart());
      charContents.get(rangeContent);

      result.put(r, new String(rangeContent));
    }

    return result;
  }

  @Override
  public void write(Text text, Reader contents) throws IOException {
    final GraphText graphText = (GraphText) text;

    CountingWriter countingWriter = new CountingWriter(Files.newWriter(new File(contentStore, graphText.getUUID()), CHARSET));
    DigestingFilterReader digestingReader = new DigestingFilterReader(contents);
    try {
      CharStreams.copy(digestingReader, countingWriter);
    } finally {
      Closeables.close(countingWriter, false);
    }

    final Node node = graphText.getNode();
    node.setProperty(GraphText.PROP_LENGTH, countingWriter.length);
    node.setProperty(GraphText.PROP_DIGEST, digestingReader.digest());
  }

  @Override
  public void write(Text text, Reader contents, long contentLength) throws IOException {
    write(text, contents);
  }
}
