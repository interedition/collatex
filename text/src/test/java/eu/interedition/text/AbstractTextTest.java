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

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringReader;

/**
 * Base class for tests using an in-memory document model.
 *
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 */
@Transactional
public abstract class AbstractTextTest extends AbstractTest {

  public static final String TEST_TEXT = "Hello World";
  @Autowired
  protected TextRepository textRepository;

  /**
   * The in-memory document model to run tests against.
   */
  protected Text text;

  /**
   * Creates a new document model before every test.
   */
  @Before
  public void createTestText() throws IOException {
    this.text = textRepository.create(new StringReader(getTestText()));
  }

  /**
   * Removes the document model.
   */
  @After
  public void cleanTestText() {
    if (text != null) {
      textRepository.delete(text);
      text = null;
    }
  }

  protected String getTestText() {
    return TEST_TEXT;
  }
}
