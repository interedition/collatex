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
package eu.interedition.text.repository.model;

import eu.interedition.text.QName;
import eu.interedition.text.util.SimpleXMLParserConfiguration;
import eu.interedition.text.xml.XMLParserModule;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLTransformation extends SimpleXMLParserConfiguration {
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

  public Set<QName> getIncluded() {
    return included;
  }

  @JsonDeserialize(contentAs = QNameImpl.class)
  public void setIncluded(Set<QName> included) {
    this.included = included;
  }

  public Set<QName> getExcluded() {
    return excluded;
  }

  @JsonDeserialize(contentAs = QNameImpl.class)
  public void setExcluded(Set<QName> excluded) {
    this.excluded = excluded;
  }

  public Set<QName> getLineElements() {
    return lineElements;
  }

  @JsonDeserialize(contentAs = QNameImpl.class)
  public void setLineElements(Set<QName> lineElements) {
    this.lineElements = lineElements;
  }

  public Set<QName> getContainerElements() {
    return containerElements;
  }

  @JsonDeserialize(contentAs = QNameImpl.class)
  public void setContainerElements(Set<QName> containerElements) {
    this.containerElements = containerElements;
  }

  public Set<QName> getNotableElements() {
    return notableElements;
  }

  @JsonDeserialize(contentAs = QNameImpl.class)
  public void setNotableElements(Set<QName> notableElements) {
    this.notableElements = notableElements;
  }

  @JsonIgnore
  @Override
  public List<XMLParserModule> getModules() {
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
