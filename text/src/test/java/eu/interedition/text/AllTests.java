/*
 * #%L
 * Text: A text model with range-based markup via standoff annotations.
 * %%
 * Copyright (C) 2010 - 2011 The Interedition Development Group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.interedition.text;

import eu.interedition.text.analysis.OverlapAnalyzerTest;
import eu.interedition.text.event.AnnotationEventSourceTest;
import eu.interedition.text.query.RangeQueryTest;
import eu.interedition.text.rdbms.AnnotationLinkTest;
import eu.interedition.text.rdbms.AnnotationTest;
import eu.interedition.text.rdbms.QNameTest;
import eu.interedition.text.rdbms.TextTest;
import eu.interedition.text.xml.XMLParserTest;
import eu.interedition.text.xml.XMLSerializerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({RangeQueryTest.class, QNameTest.class, TextTest.class, AnnotationTest.class, AnnotationLinkTest.class,
        AnnotationEventSourceTest.class, XMLParserTest.class, XMLSerializerTest.class, OverlapAnalyzerTest.class})
public class AllTests {
}
