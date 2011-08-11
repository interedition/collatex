package eu.interedition.text;

import eu.interedition.text.analysis.OverlapAnalyzerTest;
import eu.interedition.text.event.AnnotationEventSourceTest;
import eu.interedition.text.query.RangeQueryTest;
import eu.interedition.text.rdbms.AnnotationLinkTest;
import eu.interedition.text.xml.XMLParserTest;
import eu.interedition.text.xml.XMLSerializerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({RangeQueryTest.class, QNameTest.class, AnnotationTest.class, AnnotationLinkTest.class,
        AnnotationEventSourceTest.class, XMLParserTest.class, XMLSerializerTest.class, OverlapAnalyzerTest.class})
public class AllTests {
}
