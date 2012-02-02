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

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.text.AbstractTestResourceTest;
import eu.interedition.text.Annotation;
import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.event.AnnotationEventAdapter;
import eu.interedition.text.event.AnnotationEventSource;
import eu.interedition.text.query.Criteria;
import eu.interedition.text.rdbms.RelationalTextRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
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
  private AnnotationRepository annotationRepository;

  @Autowired
  private AnnotationEventSource annotationEventSource;

  private Tokenizer tokenizer;

  @Before
  public void createTokenizer() {
    tokenizer = new Tokenizer();
    tokenizer.setAnnotationRepository(annotationRepository);
    tokenizer.setEventSource(annotationEventSource);
  }

  @Test
  public void printTokenization() throws IOException {
    printTokenizedWitness(tokenize());

  }

  @Test
  public void streamTokenNGrams() throws IOException {
    final int ngramLength = 2;
    annotationEventSource.listen(new AnnotationEventAdapter() {
      private Queue<String> ngram = new ArrayDeque<String>(ngramLength);
      private Set<Integer> hashes = Sets.newHashSet();
      private int ngrams;
      
      @Override
      public void text(Range r, String text) {
        ngram.add(text.trim().toLowerCase());
        if (ngram.size() == ngramLength) {
          ngrams++;

          final int hash = Joiner.on("").join(ngram).hashCode();
          if (hashes.contains(hash)) {
            LOG.debug("{} = {}", Iterables.toString(ngram), hash);
          }
          hashes.add(hash);
          
          ngram.remove();
        }
      }

      @Override
      public void end() {
        LOG.debug("{} vs. {}", ngrams, hashes.size());
      }

    }, tokenize(), annotationName(Tokenizer.DEFAULT_TOKEN_NAME));
  }

  protected Text tokenize() throws IOException {
    final Text text = text("george-algabal-tei.xml");
    tokenizer.tokenize(text, new WhitespaceTokenizerSettings(true));
    return text;
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
