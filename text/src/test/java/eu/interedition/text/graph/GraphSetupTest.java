package eu.interedition.text.graph;

import com.google.common.collect.Iterables;
import eu.interedition.text.*;
import eu.interedition.text.mem.SimpleAnnotation;
import eu.interedition.text.mem.SimpleQName;
import eu.interedition.text.query.Criteria;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class GraphSetupTest extends AbstractGraphTest {

    @Autowired
    private AnnotationRepository annotationRepository;

    @Autowired
    private TextRepository textRepository;

    @Test
    public void simpleSetup() {
        Assert.assertNotNull(annotationRepository);
        Assert.assertNotNull(textRepository);
    }

    @Test
    public void annotateText() throws IOException {
        final Text text = textRepository.create(new StringReader("Hello World"));
        annotationRepository.create(new SimpleAnnotation(text, new SimpleQName(TextConstants.INTEREDITION_NS_URI, "test"), new Range(0, 2)));
        Assert.assertFalse(Iterables.isEmpty(annotationRepository.find(Criteria.text(text))));


    }
}
