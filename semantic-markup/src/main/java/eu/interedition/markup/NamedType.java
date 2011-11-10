package eu.interedition.markup;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;

import javax.xml.namespace.QName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class NamedType {
    protected final String namespace;
    protected final String localName;

    protected NamedType(String namespace, String localName) {
        this.namespace = Preconditions.checkNotNull(namespace);
        this.localName = Preconditions.checkNotNull(localName);
    }

    protected NamedType(QName name) {
        this(name.getNamespaceURI(), name.getLocalPart());
    }

    public String getNamespace() {
        return namespace;
    }

    public String getLocalName() {
        return localName;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(namespace, localName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AnnotationType) {
            final AnnotationType other = (AnnotationType) obj;
            return localName.equals(other.localName) && namespace.equals(other.namespace);
        }
        return super.equals(obj);
    }

    protected StringBuilder toStringBuilder() {
        return new StringBuilder("{").append(namespace).append("}").append(localName);
    }

    protected ComparisonChain comparisonChain(NamedType o) {
        return ComparisonChain.start().compare(namespace, o.namespace).compare(localName, o.localName);
    }
}
