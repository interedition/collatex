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
package eu.interedition.text.analysis;

import com.google.common.collect.Sets;
import eu.interedition.text.Annotation;
import eu.interedition.text.QName;
import eu.interedition.text.event.AnnotationEventAdapter;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class OverlapAnalyzer extends AnnotationEventAdapter {
  protected Set<QName> selfOverlapping;

  protected Set<SortedSet<QName>> overlapping;

  protected Set<Annotation> started;

  public Set<QName> getSelfOverlapping() {
    return selfOverlapping;
  }

  public Set<SortedSet<QName>> getOverlapping() {
    return overlapping;
  }

  @Override
  public void start() {
    selfOverlapping = Sets.newHashSet();
    overlapping = Sets.newHashSet();
    started = Sets.newHashSet();
  }

  @Override
  public void start(long offset, Map<Annotation, Map<QName, String>> annotations) {
    started.addAll(annotations.keySet());
  }

  @Override
  public void end(long offset, Map<Annotation, Map<QName, String>> annotations) {
    final Set<Annotation> endings = annotations.keySet();
    started.removeAll(endings);

    for (Annotation ending : endings) {
      final QName endingName = ending.getName();
      for (Annotation started : this.started) {
        final QName startedName = started.getName();
        if (!started.getRange().encloses(ending.getRange())) {
          if (startedName.equals(endingName)) {
            selfOverlapping.add(endingName);
          } else {
            overlapping.add(Sets.newTreeSet(Sets.newHashSet(startedName, endingName)));
          }
        }
      }
    }
  }
}
