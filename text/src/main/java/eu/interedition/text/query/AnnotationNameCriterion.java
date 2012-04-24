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
package eu.interedition.text.query;

import eu.interedition.text.Name;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import java.net.URI;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationNameCriterion extends QueryCriterion {
  private final Name name;

  AnnotationNameCriterion(Name name) {
    this.name = name;
  }

  @Override
  Criterion restrict() {
    final URI namespace = name.getNamespace();
    return Restrictions.and(
            Restrictions.eq("name.localName", name.getLocalName()),
            (namespace == null ? Restrictions.isNull("name.namespaceURI") : Restrictions.eq("name.namespaceURI", namespace.toString()))
    );
  }
}
