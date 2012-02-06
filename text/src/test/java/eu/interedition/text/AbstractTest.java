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

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

/**
 * Base class for tests providing utility functions.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/testContext.xml", "classpath:/eu/interedition/text/rdbms/repository-context.xml"})
@Transactional
public abstract class AbstractTest {
  /**
   * Test namespace.
   */
  protected static final URI TEST_NS = URI.create("urn:text-test-ns");

  /**
   * A logger for debug output.
   */
  protected static final Logger LOG = LoggerFactory.getLogger(AbstractTest.class.getPackage().getName());

  protected static String escapeNewlines(String str) {
    return str.replaceAll("[\n\r]+", "\\\\n");
  }
}
