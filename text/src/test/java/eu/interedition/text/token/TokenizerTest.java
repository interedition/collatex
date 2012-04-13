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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.text.AbstractTestResourceTest;
import eu.interedition.text.Annotation;
import eu.interedition.text.Name;
import eu.interedition.text.Text;
import eu.interedition.text.TextConstants;
import eu.interedition.text.TextRange;
import eu.interedition.text.TextTarget;
import eu.interedition.text.query.AnnotationListenerAdapter;
import eu.interedition.text.query.QueryCriterion;
import eu.interedition.text.query.QueryCriteria;
import org.annolab.tt4j.TokenHandler;
import org.annolab.tt4j.TreeTaggerException;
import org.annolab.tt4j.TreeTaggerWrapper;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.regex.Pattern;

import static eu.interedition.text.query.QueryCriteria.and;
import static eu.interedition.text.query.QueryCriteria.annotationName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TokenizerTest extends AbstractTestResourceTest {

  private static final Name SENTENCE_NAME = new Name(TextConstants.TEI_NS, "s");

  @Autowired
  private SessionFactory sessionFactory;

  @Test
  public void printTokenization() throws IOException, TreeTaggerException {
    final Text text = tokenize();
    printTokenizedWitness(text, annotationName(SENTENCE_NAME));

    final SortedSet<TextRange> sentences = Sets.newTreeSet();
    for (Annotation a : QueryCriteria.and(QueryCriteria.text(text), QueryCriteria.annotationName(SENTENCE_NAME)).iterate(sessionFactory.getCurrentSession())) {
      sentences.add(a.getTarget());
    }
    final TreeTaggerWrapper<String> treeTagger = new TreeTaggerWrapper<String>();
    final SortedMap<String, Integer> posStatistics = Maps.newTreeMap();
    try {
      treeTagger.setModel("/Users/gregor/tree-tagger/lib/german.par:utf-8");
      treeTagger.setPerformanceMode(true);
      treeTagger.setHandler(new TokenHandler<String>() {
        public void token(String token, String pos, String lemma) {
          posStatistics.put(pos, Objects.firstNonNull(posStatistics.get(pos), 0) + 1);
        }
      });
      for (String sentenceStr : text.read(sentences).values()) {
        treeTagger.process(Lists.newArrayList(Iterables.transform(Arrays.asList(sentenceStr.replaceAll("\\s+", " ").trim().split("\\s")), new Function<String, String>() {
          @Override
          public String apply(@Nullable String input) {
            return input.replaceAll("[\\p{Punct}]", "");
          }
        })));
      }
      for (Map.Entry<String, Integer> pos : posStatistics.entrySet()) {
        System.out.printf("%s: %d\n", pos.getKey(), pos.getValue());
      }

    }
    finally {
      treeTagger.destroy();
    }
  }

  protected Text tokenize() throws IOException {
    final Text text = text("gottsched-cato-tei.xml");
    new Tokenizer(sessionFactory, SENTENCE_NAME).tokenize(text, new TokenizerSettings() {
      private StringBuffer buf = new StringBuffer();
      @Override
      public boolean startingAnnotationsAreBoundary(Text text, long offset, Iterable<Annotation> annotations) {
        return false;
      }

      @Override
      public boolean endingAnnotationsAreBoundary(Text text, long offset, Iterable<Annotation> annotations) {
        return false;
      }

      @Override
      public boolean isBoundary(Text text, long offset, char c) {
        buf.append(c);
        if (isSentenceBoundary(c, buf.length() - 1)) {
          return true;
        } else if (isFilling(c)) {
          for (int i = buf.length() - 2; i >= 0; i--) {
            if (isSentenceBoundary(buf.charAt(i), i)) {
              return true;
            }
            if (isFilling(buf.charAt(i))) {
              continue;
            }
            break;
          }
          return false;
        }
        return false;
      }

      private boolean isSentenceBoundary(char c, int i) {
        return (c == '!' || c == '?' || c == ':' || c == ';' || (c == '.' && (i == 0 || !Character.isDigit(buf.charAt(i - 1)))));
      }

      private boolean isFilling(char c) {
        return Character.isWhitespace(c) || !Character.isLetterOrDigit(c);
      }
    });
    return text;
  }

  protected void printTokenizedWitness(Text text, QueryCriterion tokenCriterion) throws IOException {
    if (!LOG.isDebugEnabled()) {
      return;
    }

    long read = 0;

    final SortedMap<TextRange, Boolean> ranges = Maps.newTreeMap();
    for (Annotation token : Annotation.orderByText(text).immutableSortedCopy(and(QueryCriteria.text(text), tokenCriterion).iterate(sessionFactory.getCurrentSession()))) {
      final TextTarget range = token.getTarget();
      if (read < range.getStart()) {
        ranges.put(new TextRange(read, range.getStart()), false);
      }
      ranges.put(range, true);
      read = range.getEnd();
    }

    final long length = text.getLength();
    if (read < length) {
      ranges.put(new TextRange(read, (int) length), false);
    }

    final SortedMap<TextRange, String> texts = text.read(Sets.newTreeSet(ranges.keySet()));
    StringBuilder tokenized = new StringBuilder();
    for (Map.Entry<TextRange, Boolean> range : ranges.entrySet()) {
      tokenized.append(range.getValue() ? "[" : "");
      tokenized.append(texts.get(range.getKey()));
      tokenized.append(range.getValue() ? "]" : "");
    }
    LOG.debug(tokenized.toString());
  }
}
