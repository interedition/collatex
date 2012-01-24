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

import com.google.common.collect.Multimap;
import eu.interedition.text.query.Criterion;

import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public interface AnnotationLinkRepository {
  Map<AnnotationLink, Set<Annotation>> create(Multimap<Name, Set<Annotation>> links);

  Map<AnnotationLink, Set<Annotation>> find(Criterion criterion);

  void delete(Iterable<AnnotationLink> links);

  void delete(AnnotationLink... links);

  void delete(Criterion criterion);

  Map<AnnotationLink, Map<Name, String>> get(Iterable<AnnotationLink> links, Set<Name> names);

  void set(Map<AnnotationLink, Map<Name, String>> data);

  void unset(Map<AnnotationLink, Iterable<Name>> data);
}
