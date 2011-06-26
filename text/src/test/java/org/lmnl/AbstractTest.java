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

package org.lmnl;

import static org.lmnl.Annotation.LMNL_NS_URI;

import java.net.URI;
import java.util.List;
import java.util.SortedMap;

import org.junit.runner.RunWith;
import org.lmnl.util.OverlapIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Base class for tests providing utility functions.
 * 
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/testContext.xml")
public abstract class AbstractTest {
	protected static final QName XML_ANNOTATION_NAME = new QNameImpl(LMNL_NS_URI, "xml");
	protected static final QName TEXT_ANNOTATION_NAME = new QNameImpl(LMNL_NS_URI, "text");
	protected static final QName OFFSET_ANNOTATION_NAME = new QNameImpl(LMNL_NS_URI, "offsets");

	/**
	 * Test namespace.
	 */
	protected static final URI TEST_NS = URI.create("urn:lmnl-test-ns");

	/**
	 * A logger for debug output.
	 */
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractTest.class.getPackage().getName());

	/**
	 * Prints the given {@link OverlapIndexer range index} to the log.
	 * 
	 * @param index
	 *                the range index to output
	 */
	protected void printDebugMessage(SortedMap<Range, List<Annotation>> index) {
		if (LOG.isDebugEnabled()) {
			final StringBuilder str = new StringBuilder();
			for (Range segment : index.keySet()) {
				str.append("[" + segment + ": { ");
				boolean first = true;
				for (Annotation annotation : index.get(segment)) {
					if (first) {
						first = false;
					} else {
						str.append(", ");
					}
					str.append(annotation.toString());
				}
				str.append(" }]\n");
			}
			LOG.debug(str.toString());
		}
	}

	/**
	 * Prints the given message to the log.
	 * 
	 * @param msg
	 *                the debug message
	 */
	protected void printDebugMessage(String msg) {
		LOG.debug(msg);
	}
}
