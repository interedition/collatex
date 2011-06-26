package org.lmnl.rdbms;

import static javax.xml.XMLConstants.XML_NS_URI;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lmnl.QName;
import org.lmnl.QNames;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * A qualified/ "namespaced" identifier.
 * 
 * @author <a href="http://gregor.middell.net/" title="Homepage of Gregor Middell">Gregor Middell</a>
 * 
 */
public class QNameRelation implements QName {
	public static final QName COMMENT_QNAME = new QNameRelation(URI.create(XML_NS_URI), "comment");
	public static final QName COMMENT_TEXT_QNAME = new QNameRelation(URI.create(XML_NS_URI), "commentText");

	public static final QName PI_QNAME = new QNameRelation(URI.create(XML_NS_URI), "pi");
	public static final QName PI_TARGET_QNAME = new QNameRelation(URI.create(XML_NS_URI), "piTarget");
	public static final QName PI_DATA_QNAME = new QNameRelation(URI.create(XML_NS_URI), "piDarget");

	public static final QName TEXT_QNAME = new QNameRelation(URI.create(XML_NS_URI), "text");
	public static final QName TEXT_LINE_ATTR_QNAME = new QNameRelation(URI.create(XML_NS_URI), "textLine");
	public static final QName TEXT_COLUMN_ATTR_QNAME = new QNameRelation(URI.create(XML_NS_URI), "textColumn");
	public static final QName TEXT_OFFSET_ATTR_QNAME = new QNameRelation(URI.create(XML_NS_URI), "textOffset");

	private static final Pattern STR_REPR = Pattern.compile("^\\{([^\\}]*)\\}(.+)$");

	private int id;
	private URI namespace;
	private String localName;

	public QNameRelation() {
	}

	public QNameRelation(int id, URI namespace, String localName) {
		this.id = id;
		this.namespace = namespace;
		this.localName = localName;
	}

	public QNameRelation(URI namespace, String localName) {
		this(0, namespace, localName);
	}

	public QNameRelation(String uri, String localName, String qName) {
		this.id = 0;
		if (uri.length() == 0 && localName.length() == 0) {
			this.namespace = null;
			this.localName = qName;
		} else {
			this.namespace = (Strings.isNullOrEmpty(uri) ? null : URI.create(uri));
			this.localName = localName;
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
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
		return new QNameRelation(Strings.isNullOrEmpty(ns) ? null : URI.create(ns), matcher.group(2));
	}

	public int compareTo(QName o) {
		return QNames.COMPARATOR.compare(this, o);
	}
}
