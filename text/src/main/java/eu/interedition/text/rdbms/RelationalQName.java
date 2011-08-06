package eu.interedition.text.rdbms;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import eu.interedition.text.QName;
import eu.interedition.text.util.QNames;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A qualified/ "namespaced" identifier.
 *
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 */
public class RelationalQName implements QName {
  private static final Pattern STR_REPR = Pattern.compile("^\\{([^\\}]*)\\}(.+)$");

  private long id;
  private URI namespace;
  private String localName;

  public RelationalQName() {
  }

  public RelationalQName(long id, URI namespace, String localName) {
    this.id = id;
    this.namespace = namespace;
    this.localName = localName;
  }

  public RelationalQName(long id, QName other) {
    this(id, other.getNamespaceURI(), other.getLocalName());
  }

  public RelationalQName(URI namespace, String localName) {
    this(0, namespace, localName);
  }

  public RelationalQName(String uri, String localName, String qName) {
    this.id = 0;
    if (uri.length() == 0 && localName.length() == 0) {
      this.namespace = null;
      this.localName = qName;
    } else {
      this.namespace = (Strings.isNullOrEmpty(uri) ? null : URI.create(uri));
      this.localName = localName;
    }
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public URI getNamespaceURI() {
    return namespace;
  }

  public void setNamespaceURI(URI namespace) {
    this.namespace = namespace;
  }

  public String getNamespace() {
    return (this.namespace == null ? null : this.namespace.toString());
  }

  public void setNamespace(String namespace) {
    this.namespace = (namespace == null ? null : URI.create(namespace));
  }

  public String getLocalName() {
    return localName;
  }

  public void setLocalName(String localName) {
    this.localName = localName;
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

  @Override
  public String toString() {
    return QNames.toString(this);
  }

  public static QName fromString(String str) {
    final Matcher matcher = STR_REPR.matcher(str);
    Preconditions.checkArgument(matcher.matches());

    final String ns = matcher.group(1);
    return new RelationalQName(Strings.isNullOrEmpty(ns) ? null : URI.create(ns), matcher.group(2));
  }

  public int compareTo(QName o) {
    return QNames.COMPARATOR.compare(this, o);
  }
}
