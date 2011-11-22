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
package eu.interedition.text.rdbms;

import eu.interedition.text.AbstractTextTest;
import eu.interedition.text.Text;
import eu.interedition.text.util.TextDigestingFilterReader;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.junit.Test;

import static eu.interedition.text.util.TextDigestingFilterReader.digest;
import static org.apache.commons.codec.binary.Hex.encodeHexString;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextTest extends AbstractTextTest {
  @Test
  public void digesting() throws Exception {
    Assert.assertEquals(encodeHexString(digest(TEST_TEXT)), encodeHexString(text.getDigest()));

    final Text concat = textRepository.concat(text, text, text);
    Assert.assertEquals(3 * text.getLength(), concat.getLength());
  }
}
