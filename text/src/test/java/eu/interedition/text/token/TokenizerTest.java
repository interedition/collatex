/*
 * #%L
 * Text Repository: Datastore for texts based on Interedition's model.
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
package eu.interedition.text.token;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.text.*;
import eu.interedition.text.event.AnnotationEventSource;
import eu.interedition.text.query.Criteria;
import eu.interedition.text.rdbms.RelationalTextRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;

import static eu.interedition.text.query.Criteria.and;
import static eu.interedition.text.query.Criteria.annotationName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Transactional
public class TokenizerTest extends AbstractTestResourceTest {
  @Autowired
  private RelationalTextRepository textRepository;

  @Autowired
  private AnnotationEventSource annotationEventSource;

  @Autowired
  private AnnotationRepository annotationRepository;

  @Test
  public void tokenize() throws IOException, TransformerException, XMLStreamException {
    final Tokenizer tokenizer = new Tokenizer();
    tokenizer.setAnnotationRepository(annotationRepository);
    tokenizer.setEventSource(annotationEventSource);

    final Text text = text("george-algabal-tei.xml");
    tokenizer.tokenize(text, new WhitespaceTokenizerSettings(true));
    printTokenizedWitness(text);
  }

  private void printTokenizedWitness(Text text) throws IOException {
    if (!LOG.isDebugEnabled()) {
      return;
    }

    long read = 0;

    final SortedMap<Range, Boolean> ranges = Maps.newTreeMap();
    for (Annotation token : annotationRepository.find(and(Criteria.text(text), annotationName(Tokenizer.DEFAULT_TOKEN_NAME)))) {
      final Range range = token.getRange();
      if (read < range.getStart()) {
        ranges.put(new Range(read, range.getStart()), false);
      }
      ranges.put(token.getRange(), true);
      read = token.getRange().getEnd();
    }

    final long length = text.getLength();
    if (read < length) {
      ranges.put(new Range(read, (int) length), false);
    }

    final SortedMap<Range, String> texts = textRepository.bulkRead(text, Sets.newTreeSet(ranges.keySet()));
    StringBuilder tokenized = new StringBuilder();
    for (Map.Entry<Range, Boolean> range : ranges.entrySet()) {
      tokenized.append(range.getValue() ? "[" : "");
      tokenized.append(texts.get(range.getKey()));
      tokenized.append(range.getValue() ? "]" : "");
    }
    LOG.debug(tokenized.toString());
  }
}
