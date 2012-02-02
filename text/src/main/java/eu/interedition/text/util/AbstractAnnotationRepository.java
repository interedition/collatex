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
package eu.interedition.text.util;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.interedition.text.*;
import eu.interedition.text.mem.SimpleName;
import eu.interedition.text.query.Criterion;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static com.google.common.base.Functions.compose;
import static eu.interedition.text.transform.AnnotationTransformers.adopt;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class AbstractAnnotationRepository implements AnnotationRepository, InitializingBean {

  protected TextRepository textRepository;
  protected SAXParserFactory saxParserFactory;

  protected int batchSize = 10000;

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public Iterable<Annotation> create(Annotation... annotations) {
    return create(Arrays.asList(annotations));
  }

  public void delete(Annotation... annotations) {
    delete(Arrays.asList(annotations));
  }

  @Override
  public Iterable<Annotation> find(Criterion criterion) {
    final SortedSet<Annotation> result = Sets.newTreeSet();
    scroll(criterion, new AnnotationConsumer() {
      @Override
      public void consume(Annotation annotation) {
        result.add(annotation);
      }
    });
    return result;
  }

  public SortedSet<Name> names(Text text) {
    final SortedSet<Name> names = getNames(text);

    if (names.isEmpty() && text.getType() == Text.Type.XML) {
      try {
        textRepository.read(text, new TextConsumer() {
          public void read(Reader content, long contentLength) throws IOException {
            if (contentLength == 0) {
              return;
            }
            try {
              saxParserFactory.newSAXParser().parse(new InputSource(content), new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                  names.add(new SimpleName(uri, localName));
                }
              });
            } catch (SAXException e) {
              throw Throwables.propagate(e);
            } catch (ParserConfigurationException e) {
              throw Throwables.propagate(e);
            }
          }
        });
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }

    return names;
  }

  @Override
  public void transform(Criterion criterion, final Text to, final Function<Annotation, Annotation> transform) {
    final List<Annotation> batch = Lists.newArrayListWithExpectedSize(batchSize);
    scroll(criterion, new AnnotationConsumer() {
      @Override
      public void consume(Annotation annotation) {
        batch.add(annotation);
        if ((batch.size() % batchSize) == 0) {
          transform(batch, to, transform);
          batch.clear();
        }
      }
    });
    if (!batch.isEmpty()) {
      transform(batch, to, transform);
    }
  }

  @Override
  public Iterable<Annotation> transform(Iterable<Annotation> annotations, Text to, Function<Annotation, Annotation> transform) {
    return create(Lists.newArrayList(Iterables.transform(annotations, compose(transform, adopt(to)))));
  }

  protected abstract SortedSet<Name> getNames(Text text);

  @Required
  public void setTextRepository(TextRepository textRepository) {
    this.textRepository = textRepository;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.saxParserFactory = SAXParserFactory.newInstance();
    this.saxParserFactory.setNamespaceAware(true);
    this.saxParserFactory.setValidating(false);
  }
}
