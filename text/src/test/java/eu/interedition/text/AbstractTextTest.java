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

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.StringReader;

/**
 * Base class for tests using an in-memory document model.
 *
 * @author <a href="http://gregor.middell.net/"
 *         title="Homepage of Gregor Middell">Gregor Middell</a>
 */
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
