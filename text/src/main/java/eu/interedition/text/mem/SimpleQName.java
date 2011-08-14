package eu.interedition.text.mem;

import com.google.common.base.Strings;
import eu.interedition.text.QName;
import eu.interedition.text.util.QNames;

import java.net.URI;

public class SimpleQName implements QName {
  private final URI namespace;
  private final String localName;

  public SimpleQName(URI namespace, String localName) {
    this.namespace = namespace;
    this.localName = localName;
  }

  public SimpleQName(String namespace, String localName) {
    this(namespace == null ? null : URI.create(namespace), localName);
  }

  public SimpleQName(javax.xml.namespace.QName name) {
    final String ns = name.getNamespaceURI();
    this.namespace = Strings.isNullOrEmpty(ns) ? null : URI.create(ns);
    this.localName = name.getLocalPart();
  }

  public URI getNamespaceURI() {
    return namespace;
  }

  public String getLocalName() {
    return localName;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof QName) {
      return QNames.equal(this, (QName) obj);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return QNames.hashCode(this);
  }

  public int compareTo(QName o) {
    return QNames.COMPARATOR.compare(this, o);
  }

  @Override
  public String toString() {
    return QNames.toString(this);
  }
}