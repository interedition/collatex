package eu.interedition.text.repository;

import eu.interedition.text.QName;
import eu.interedition.text.rdbms.RelationalQName;
import eu.interedition.text.util.QNames;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonValue;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.net.URI;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class QNameBean implements QName {
  private String id;
  private URI namespaceURI;
  private String localName;

  public QNameBean() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @JsonProperty("ns")
  @Override
  public URI getNamespaceURI() {
    return namespaceURI;
  }

  @JsonProperty("ns")
  public void setNamespaceURI(URI namespaceURI) {
    this.namespaceURI = namespaceURI;
  }

  @JsonProperty("n")
  @Override
  public String getLocalName() {
    return localName;
  }

  @JsonProperty("n")
  public void setLocalName(String localName) {
    this.localName = localName;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof QName) {
      return QNames.equal(this, (QName) obj);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return QNames.hashCode(this);
  }

  @Override
  public int compareTo(QName o) {
    return QNames.COMPARATOR.compare(this, o);
  }
}
