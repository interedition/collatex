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
package eu.interedition.web.text;

import eu.interedition.text.Name;
import eu.interedition.text.util.SimpleXMLTransformerConfiguration;
import eu.interedition.text.xml.XMLTransformerModule;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLTransformation extends SimpleXMLTransformerConfiguration {
  protected boolean transformTEI = true;
  protected boolean removeEmpty = false;

  public boolean isTransformTEI() {
    return transformTEI;
  }

  public void setTransformTEI(boolean transformTEI) {
    this.transformTEI = transformTEI;
  }

  public boolean isRemoveEmpty() {
    return removeEmpty;
  }

  public void setRemoveEmpty(boolean removeEmpty) {
    this.removeEmpty = removeEmpty;
  }

  public Set<Name> getIncluded() {
    return included;
  }

  @JsonDeserialize(contentAs = Name.class)
  public void setIncluded(Set<Name> included) {
    this.included = included;
  }

  public Set<Name> getExcluded() {
    return excluded;
  }

  @JsonDeserialize(contentAs = Name.class)
  public void setExcluded(Set<Name> excluded) {
    this.excluded = excluded;
  }

  public Set<Name> getLineElements() {
    return lineElements;
  }

  @JsonDeserialize(contentAs = Name.class)
  public void setLineElements(Set<Name> lineElements) {
    this.lineElements = lineElements;
  }

  public Set<Name> getContainerElements() {
    return containerElements;
  }

  @JsonDeserialize(contentAs = Name.class)
  public void setContainerElements(Set<Name> containerElements) {
    this.containerElements = containerElements;
  }

  public Set<Name> getNotableElements() {
    return notableElements;
  }

  @JsonDeserialize(contentAs = Name.class)
  public void setNotableElements(Set<Name> notableElements) {
    this.notableElements = notableElements;
  }

  @JsonIgnore
  @Override
  public List<XMLTransformerModule> getModules() {
    return super.getModules();
  }

  @JsonIgnore
  @Override
  public int getTextBufferSize() {
    return super.getTextBufferSize();
  }

  @JsonIgnore
  @Override
  public void setTextBufferSize(int textBufferSize) {
    super.setTextBufferSize(textBufferSize);
  }
}
