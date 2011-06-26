package org.lmnl;

import static javax.xml.XMLConstants.XML_NS_URI;

import java.io.Serializable;
import java.net.URI;

import javax.xml.XMLConstants;

import com.google.common.base.Strings;

public class QNameImpl implements QName, Serializable {
	private static final long serialVersionUID = 1L;

	public static final QName XML_SPACE = new QNameImpl(XMLConstants.XML_NS_URI, "space");

	public static final QName COMMENT_QNAME = new QNameImpl(XML_NS_URI, "comment");
	public static final QName COMMENT_TEXT_QNAME = new QNameImpl(XML_NS_URI, "commentText");

	public static final QName PI_QNAME = new QNameImpl(XML_NS_URI, "pi");
	public static final QName PI_TARGET_QNAME = new QNameImpl(XML_NS_URI, "piTarget");
	public static final QName PI_DATA_QNAME = new QNameImpl(XML_NS_URI, "piDarget");

	public static final QName TEXT_QNAME = new QNameImpl(XML_NS_URI, "text");

	private final URI namespace;
	private final String localName;

	public QNameImpl(URI namespace, String localName) {
		this.namespace = namespace;
		this.localName = localName;
	}
	
	public QNameImpl(String namespace, String localName) {
		this(namespace == null ? null : URI.create(namespace), localName);
	}

	public QNameImpl(javax.xml.namespace.QName name) {
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
}