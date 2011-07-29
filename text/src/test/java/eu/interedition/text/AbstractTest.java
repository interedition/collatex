/**
 * Layered Markup and Annotation Language for Java (lmnl4j):
 * implementation of LMNL, a markup language supporting layered and/or
 * overlapping annotations.
 *
 * Copyright (C) 2010 the respective authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.text;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;

/**
 * Base class for tests providing utility functions.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/testContext.xml", "classpath:/eu/interedition/text/rdbms/repository-context.xml"})
public abstract class AbstractTest {
  /**
   * Test namespace.
   */
  protected static final URI TEST_NS = URI.create("urn:text-test-ns");

  /**
   * A logger for debug output.
   */
  protected static final Logger LOG = LoggerFactory.getLogger(AbstractTest.class.getPackage().getName());

  /**
   * Prints the given message to the log.
   *
   * @param msg the debug message
   */
  protected void printDebugMessage(String msg) {
    LOG.debug(msg);
  }

  protected static String escapeNewlines(String str) {
    return str.replaceAll("[\n\r]+", "\\\\n");
  }
}
