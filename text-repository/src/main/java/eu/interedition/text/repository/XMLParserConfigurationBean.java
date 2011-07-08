package eu.interedition.text.repository;

import eu.interedition.text.QName;
import eu.interedition.text.xml.NodePathHandler;
import eu.interedition.text.xml.OffsetDeltaHandler;
import eu.interedition.text.xml.SimpleXMLParserConfiguration;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLParserConfigurationBean extends SimpleXMLParserConfiguration {

  public Set<QName> getIncluded() {
    return included;
  }

  @JsonDeserialize(contentAs = QNameBean.class)
  public void setIncluded(Set<QName> included) {
    this.included = included;
  }

  public Set<QName> getExcluded() {
    return excluded;
  }

  @JsonDeserialize(contentAs = QNameBean.class)
  public void setExcluded(Set<QName> excluded) {
    this.excluded = excluded;
  }

  public Set<QName> getLineElements() {
    return lineElements;
  }

  @JsonDeserialize(contentAs = QNameBean.class)
  public void setLineElements(Set<QName> lineElements) {
    this.lineElements = lineElements;
  }

  public Set<QName> getContainerElements() {
    return containerElements;
  }

  @JsonDeserialize(contentAs = QNameBean.class)
  public void setContainerElements(Set<QName> containerElements) {
    this.containerElements = containerElements;
  }

  public Set<QName> getNotableElements() {
    return notableElements;
  }

  @JsonDeserialize(contentAs = QNameBean.class)
  public void setNotableElements(Set<QName> notableElements) {
    this.notableElements = notableElements;
  }

  @JsonIgnore
  @Override
  public void setNodePathHandler(NodePathHandler nodePathHandler) {
    super.setNodePathHandler(nodePathHandler);
  }

  @JsonIgnore
  @Override
  public void setOffsetDeltaHandler(OffsetDeltaHandler offsetDeltaHandler) {
    super.setOffsetDeltaHandler(offsetDeltaHandler);
  }
}
