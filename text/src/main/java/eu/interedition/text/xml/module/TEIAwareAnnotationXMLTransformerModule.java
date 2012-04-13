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
package eu.interedition.text.xml.module;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import eu.interedition.text.Annotation;
import eu.interedition.text.Name;
import eu.interedition.text.TextTarget;
import eu.interedition.text.xml.XMLEntity;
import eu.interedition.text.xml.XMLTransformer;
import org.codehaus.jackson.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;

import static eu.interedition.text.TextConstants.TEI_NS;
import static eu.interedition.text.TextConstants.XML_ID_ATTR_NAME;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TEIAwareAnnotationXMLTransformerModule extends AbstractAnnotationXMLTransformerModule {
  private static final Map<Name, Name> MILESTONE_ELEMENT_UNITS = Maps.newHashMap();

  static {
    MILESTONE_ELEMENT_UNITS.put(new Name(TEI_NS, "pb"), new Name(TEI_NS, "page"));
    MILESTONE_ELEMENT_UNITS.put(new Name(TEI_NS, "lb"), new Name(TEI_NS, "line"));
    MILESTONE_ELEMENT_UNITS.put(new Name(TEI_NS, "cb"), new Name(TEI_NS, "column"));
    MILESTONE_ELEMENT_UNITS.put(new Name(TEI_NS, "gb"), new Name(TEI_NS, "gathering"));
  }

  private static final Name MILESTONE_NAME = new Name(TEI_NS, "milestone");
  private static final Name MILESTONE_UNIT_ATTR_NAME = new Name(TEI_NS, "unit");

  private Multimap<String, Annotation> spanning;
  private Map<Name, Annotation> milestones;

  public TEIAwareAnnotationXMLTransformerModule(int batchSize) {
    super(batchSize, false);
  }

  @Override
  public void start(XMLTransformer transformer) {
    super.start(transformer);
    this.spanning = ArrayListMultimap.create();
    this.milestones = Maps.newHashMap();
  }

  @Override
  public void end(XMLTransformer transformer) {
    final long textOffset = transformer.getTextOffset();
    for (Name milestoneUnit : milestones.keySet()) {
      final Annotation last = milestones.get(milestoneUnit);
      Iterables.getOnlyElement(last.getTargets()).setEnd(textOffset);
      add(transformer, last);
    }

    this.milestones = null;
    this.spanning = null;

    super.end(transformer);
  }

  @Override
  public void start(XMLTransformer transformer, XMLEntity entity) {
    super.start(transformer, entity);
    handleSpanningElements(entity, transformer);
    handleMilestoneElements(entity, transformer);
  }

  protected void handleMilestoneElements(XMLEntity entity, XMLTransformer state) {
    final Name entityName = entity.getName();
    final ObjectNode entityAttributes = entity.getAttributes();

    Name milestoneUnit = null;
    if (MILESTONE_NAME.equals(entityName)) {
      for (Iterator<String> it = entityAttributes.getFieldNames(); it.hasNext(); ) {
        final String attrName = it.next();
        if (MILESTONE_UNIT_ATTR_NAME.getLocalName().equals(attrName) || MILESTONE_UNIT_ATTR_NAME.toString().equals(attrName)) {
          milestoneUnit = new Name(TEI_NS, entityAttributes.get(attrName).toString());
          it.remove();
        }
      }
    } else if (MILESTONE_ELEMENT_UNITS.containsKey(entityName)) {
      milestoneUnit = MILESTONE_ELEMENT_UNITS.get(entityName);
    }

    if (milestoneUnit == null) {
      return;
    }

    final long textOffset = state.getTextOffset();

    final Annotation last = milestones.get(milestoneUnit);
    if (last != null) {
      Iterables.getOnlyElement(last.getTargets()).setEnd(textOffset);
      add(state, last);
    }

    milestones.put(milestoneUnit, new Annotation(milestoneUnit, new TextTarget(state.getTarget(), textOffset, textOffset), entityAttributes));
  }

  protected void handleSpanningElements(XMLEntity entity, XMLTransformer state) {
    final ObjectNode entityAttributes = entity.getAttributes();
    String spanTo = null;
    String refId = null;
    for (Iterator<String> it = entityAttributes.getFieldNames(); it.hasNext(); ) {
      final String attrName = it.next();
      if (attrName.endsWith("spanTo")) {
        spanTo = entityAttributes.get(attrName).toString().replaceAll("^#", "");
        it.remove();
      } else if (XML_ID_ATTR_NAME.toString().equals(attrName)) {
        refId = entityAttributes.get(attrName).toString();
      }
    }

    if (spanTo == null && refId == null) {
      return;
    }

    final long textOffset = state.getTextOffset();

    if (spanTo != null) {
      final Name name = entity.getName();
      spanning.put(spanTo, new Annotation(
              new Name(name.getNamespace(), name.getLocalName().replaceAll("Span$", "")),
              new TextTarget(state.getTarget(),textOffset, textOffset),
              entityAttributes));
    }
    if (refId != null) {
      final Iterator<Annotation> aIt = spanning.removeAll(refId).iterator();
      while (aIt.hasNext()) {
        final Annotation a = aIt.next();
        Iterables.getOnlyElement(a.getTargets()).setEnd(textOffset);
        add(state, a);
      }
    }
  }
}
