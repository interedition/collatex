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

package eu.interedition.text.util;

import org.junit.Test;
import eu.interedition.text.AbstractDefaultDocumentTest;
import eu.interedition.text.AnnotationRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests the calculation of overlap indizes.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class OverlapIndexerTest extends AbstractDefaultDocumentTest {

	@Autowired
	private AnnotationRepository annotationRepository;
	
	/**
	 * Indexes a very simple document.
	 */
	@Test
	public void indexSimpleDocument() {
		addTestAnnotation("a", 0, 2);
		addTestAnnotation("b", 1, 4);
		addTestAnnotation("c", 0, 1);
		addTestAnnotation("d", 0, 6);
		addTestAnnotation("e", 2, 3);

		printDebugMessage(document.toString());
		printDebugMessage(new OverlapIndexer().apply(annotationRepository.find(document)));
	}

	@Override
	protected String documentText() {
		return "abcdef";
	}
}
