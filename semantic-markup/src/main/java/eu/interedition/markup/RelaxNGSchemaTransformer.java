package eu.interedition.markup;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.kohsuke.rngom.digested.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RelaxNGSchemaTransformer extends DPatternWalker implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(RelaxNGSchemaTransformer.class);

    private DPattern sourcePattern;
    private Deque<String> refs;
    private Deque<AnnotationType> containers;
    private Map<QName, AnnotationType> annotationTypes;
    private AnnotationDataType annotationDataType;

    public RelaxNGSchemaTransformer() {
        this(null);
    }

    public RelaxNGSchemaTransformer(DPattern sourcePattern) {
        setSourcePattern(sourcePattern);
        reset();
    }

    public Map<QName, AnnotationType> getAnnotationTypes() {
        return annotationTypes;
    }

    private void reset() {
        annotationTypes = Maps.newHashMap();
        containers = new ArrayDeque<AnnotationType>();
        refs = new ArrayDeque<String>();
    }

    public void setSourcePattern(DPattern sourcePattern) {
        this.sourcePattern = sourcePattern;
    }

    @Override
    public Void onAttribute(DAttributePattern p) {
        Void result = null;
        if (!containers.isEmpty()) {
            final AnnotationType container = containers.peek();
            for (QName name : p.getName().listNames()) {
                annotationDataType = new AnnotationDataType(name);
                annotationDataType.setDocumentation(toString(p.getAnnotation()));
                result = super.onAttribute(p);
                container.getDataTypes().add(annotationDataType);
                annotationDataType = null;
            }
        }
        return result;
    }

    @Override
    public Void onText(DTextPattern p) {
        if (!containers.isEmpty()) {
            containers.peek().setTextContainer(true);
        }
        return super.onText(p);
    }

    @Override
    public Void onElement(DElementPattern p) {
        Void result = null;
        for (QName name : p.getName().listNames()) {
            AnnotationType annotationType = annotationTypes.get(name);
            if (annotationType == null) {
                annotationTypes.put(name, annotationType = new AnnotationType(name));
                annotationType.setDocumentation(toString(p.getAnnotation()));
            }
            final AnnotationType container = containers.peek();
            if (container == null || !annotationType.getContainers().contains(container)) {
                if (container != null) {
                    annotationType.getContainers().add(container);
                }
                containers.push(annotationType);
                result = super.onElement(p);
                containers.pop();
            }
        }
        return result;
    }

    @Override
    public Void onRef(DRefPattern p) {
        final String target = p.getTarget().getName();
        if (refs.contains(target)) {
            return null;
        } else {
            refs.push(target);
            final Void result = super.onRef(p);
            refs.pop();
            return result;
        }
    }

    private String toString(DAnnotation annotation) {
        StringBuilder str = new StringBuilder();
        for (Element e : annotation.getChildren()) {
              str.append("\n").append(e.getTextContent());
        }
        return Strings.emptyToNull(str.toString().replaceAll("[\\p{Space}]+", " ").trim());
    }

    @Override
    public void run() {
        Preconditions.checkState(sourcePattern != null, "Source Pattern required");
        reset();
        sourcePattern.accept(this);
    }
}
