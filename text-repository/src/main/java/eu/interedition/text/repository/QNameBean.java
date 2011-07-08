package eu.interedition.text.repository;

import eu.interedition.text.QName;
import eu.interedition.text.util.QNames;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.net.URI;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class QNameBean implements QName {
  private URI namespaceURI;
  private String localName;

  public String getNs() {
    return (namespaceURI == null ? null : namespaceURI.toString());
  }

  public void setNs(String ns) {
    namespaceURI = (ns == null ? null : URI.create(ns));
  }

  @JsonIgnore
  @Override
  public URI getNamespaceURI() {
    return namespaceURI;
  }

  @JsonIgnore
  public void setNamespaceURI(URI namespaceURI) {
    this.namespaceURI = namespaceURI;
  }

  @Override
  public String getLocalName() {
    return localName;
  }

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
