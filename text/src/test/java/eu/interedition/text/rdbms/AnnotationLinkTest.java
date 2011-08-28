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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import eu.interedition.text.*;
import eu.interedition.text.mem.SimpleAnnotation;
import eu.interedition.text.mem.SimpleQName;
import eu.interedition.text.query.Criteria;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationLinkTest extends AbstractTestResourceTest {

  @Autowired
  private AnnotationLinkRepository annotationLinkRepository;

  @Autowired
  private DataSource dataSource;

  @Test
  public void cleanup() throws Exception {
    final Text text = text();
    Annotation textAnnotation = Iterables.getFirst(annotationRepository.find(Criteria.text(text)), null);

    final Text source = source();
    final Annotation sourceAnnotation = Iterables.getOnlyElement(annotationRepository.create(new SimpleAnnotation(source, new SimpleQName(TEST_NS, "test"), new Range(0, (int) source.getLength()))));

    final Multimap<QName, Set<Annotation>> createdLinks = ArrayListMultimap.create();
    createdLinks.put(new SimpleQName(TEST_NS, "testLink"), Sets.newHashSet(textAnnotation, sourceAnnotation));
    annotationLinkRepository.create(createdLinks);

    final Map<AnnotationLink, Set<Annotation>> beforeTextRemoval = annotationLinkRepository.find(Criteria.any());
    Assert.assertEquals(1, beforeTextRemoval.size());

    textRepository.delete(source);
    textRepository.delete(text);

    final Map<AnnotationLink, Set<Annotation>> afterTextRemoval = annotationLinkRepository.find(Criteria.any());
    Assert.assertEquals(0, afterTextRemoval.size());

    final SimpleJdbcTemplate jt = new SimpleJdbcTemplate(dataSource);
    Assert.assertEquals(1, jt.queryForInt("select count(*) from text_annotation_link"));
    ((RelationalAnnotationLinkRepository)annotationLinkRepository).cleanup();
    Assert.assertEquals(0, jt.queryForInt("select count(*) from text_annotation_link"));

  }
}
