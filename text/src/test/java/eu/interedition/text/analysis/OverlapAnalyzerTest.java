package eu.interedition.text.analysis;

import com.google.common.collect.Iterables;
import eu.interedition.text.AbstractTestResourceTest;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.event.AnnotationEventSource;
import eu.interedition.text.mem.SimpleAnnotation;
import eu.interedition.text.mem.SimpleQName;
import eu.interedition.text.query.Criteria;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class OverlapAnalyzerTest extends AbstractTestResourceTest {

  @Autowired
  private AnnotationEventSource eventSource;

  @Test
  public void analyzeNonOverlap() throws IOException {
    final OverlapAnalyzer analyzer = analyze(text());
    Assert.assertEquals(0, analyzer.getOverlapping().size());
    Assert.assertEquals(0, analyzer.getSelfOverlapping().size());
  }

  @Test
  public void analyzeSelfOverlap() throws IOException {
    final SimpleQName overlap = new SimpleQName(TEST_NS, "overlap");
    annotationRepository.create(
            new SimpleAnnotation(text, overlap, new Range(0, TEST_TEXT.length() - 1)),
            new SimpleAnnotation(text, overlap, new Range(1, TEST_TEXT.length()))
    );
    final OverlapAnalyzer analyzer = analyze(text);
    Assert.assertEquals(0, analyzer.getOverlapping().size());
    Assert.assertEquals(1, analyzer.getSelfOverlapping().size());
    Assert.assertEquals(overlap, Iterables.getOnlyElement(analyzer.getSelfOverlapping()));
  }

  protected OverlapAnalyzer analyze(Text text) throws IOException {
    final OverlapAnalyzer analyzer = new OverlapAnalyzer();
    eventSource.listen(analyzer, text, Criteria.any());
    return analyzer;
  }
}
