package eu.interedition.text.repository;

import com.google.common.base.Throwables;
import eu.interedition.text.Text;
import eu.interedition.text.rdbms.RelationalTextRepository;
import eu.interedition.text.repository.textindex.TextIndex;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class IndexingTextRepository extends RelationalTextRepository {

  @Autowired
  private TextIndex textIndex;

  @Override
  public void delete(Text text) {
    try {
      super.delete(text);
      textIndex.delete(text);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void write(Text text, Reader contents, long contentLength) throws IOException {
    super.write(text, contents, contentLength);
    textIndex.update(text);
  }
}
