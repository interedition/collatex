package eu.interedition.markup;

import com.google.common.collect.Sets;

import javax.xml.namespace.QName;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationType extends NamedType implements Comparable<AnnotationType> {
    private final SortedSet<AnnotationDataType> dataTypes = Sets.newTreeSet();
    private final SortedSet<AnnotationType> containers = Sets.newTreeSet();
    private boolean textContainer;
    private String documentation;

    public AnnotationType(String namespace, String localName) {
        super(namespace, localName);
    }

    public AnnotationType(QName name) {
        super(name);
    }

    public SortedSet<AnnotationDataType> getDataTypes() {
        return dataTypes;
    }

    public SortedSet<AnnotationType> getContainers() {
        return containers;
    }

    public boolean isTextContainer() {
        return textContainer;
    }

    public void setTextContainer(boolean textContainer) {
        this.textContainer = textContainer;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    @Override
    public int compareTo(AnnotationType o) {
        return comparisonChain(o).result();
    }

    @Override
    public String toString() {
        return toStringBuilder().append(textContainer ? "[TXT]" : "").toString();
    }
}
