package eu.interedition.markup;

import javax.xml.namespace.QName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationDataType extends NamedType implements Comparable<AnnotationDataType> {
    private String documentation;

    public AnnotationDataType(String namespace, String localName) {
        super(namespace, localName);
    }

    public AnnotationDataType(QName name) {
        super(name);
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    @Override
    public int compareTo(AnnotationDataType o) {
        return comparisonChain(o).result();
    }

    @Override
    public String toString() {
        return toStringBuilder().toString();
    }
}
