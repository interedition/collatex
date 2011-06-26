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

package org.lmnl.util;

import org.lmnl.Annotation;
import org.lmnl.Range;

/**
 * Callback interface implemented by objects, which generate unique
 * <code>xml:id</code> values for XStandoff <i>segments</i>.
 * 
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public interface IdGenerator {
	/**
	 * Returns a unique <code>xml:id</code> value for a given segment.
	 * 
	 * @param range
	 *                the text segment's address, for which an identifier is
	 *                needed
	 * @return <code>xml:id</code>-compliant identifier value
	 */
	String next(Annotation annotation);

	String next(Range address);

	void reset();
}
