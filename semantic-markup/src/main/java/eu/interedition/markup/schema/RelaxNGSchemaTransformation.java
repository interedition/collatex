package eu.interedition.markup.schema;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import eu.interedition.markup.name.Name;
import eu.interedition.markup.name.NameManager;
import eu.interedition.markup.name.Namespace;
import eu.interedition.markup.type.AnnotationType;
import eu.interedition.markup.type.PropertyType;
import eu.interedition.markup.type.TypeManager;
import org.kohsuke.rngom.digested.*;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.xml.SAXParseable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Service
@Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
public class RelaxNGSchemaTransformation extends DPatternWalker {
    @Autowired
    private NameManager nameManager;

    @Autowired
    private TypeManager typeManager;

    private final Deque<String> deferenceStack = new ArrayDeque<String>();
    private final Deque<AnnotationType> nestingStack = new ArrayDeque<AnnotationType>();

    private final Map<javax.xml.namespace.QName, AnnotationType> annotationTypeCache = Maps.newHashMap();
    private final Map<javax.xml.namespace.QName, PropertyType> annotationDataTypeCache = Maps.newHashMap();
    private final Map<String, Namespace> namespaceCache = Maps.newHashMap();
    private final Map<javax.xml.namespace.QName, Name> nameCache = Maps.newHashMap();

    private final Multimap<AnnotationType, AnnotationType> containers = HashMultimap.create();
    private final Multimap<AnnotationType, PropertyType> data = HashMultimap.create();

    private PropertyType propertyType;
    private Schema schema;
    private InputSource inputSource;
    private InputStream inputStream;
    private DPattern sourcePattern;

    @Override
    public Void onAttribute(DAttributePattern p) {
        Void result = null;
        if (!nestingStack.isEmpty()) {
            final AnnotationType container = nestingStack.peek();
            for (javax.xml.namespace.QName xmlName : p.getName().listNames()) {
                propertyType = annotationDataTypeCache.get(xmlName);
                if (propertyType == null) {
                    annotationDataTypeCache.put(xmlName, propertyType = typeManager.createPropertyType(name(xmlName), toString(p.getAnnotation())));
                }

                result = super.onAttribute(p);
                data.put(container, propertyType);
                propertyType = null;
            }
        }
        return result;
    }

    public RelaxNGSchemaTransformation with(InputSource inputSource, Schema schema) {
        this.schema = schema;
        this.inputSource = inputSource;
        return this;
    }

    public void execute() throws IllegalSchemaException {
        ((DPattern) new SAXParseable(inputSource, new DefaultHandler()).parse(new DSchemaBuilderImpl())).accept(this);
        for (AnnotationType at : containers.keySet()) {
            for (AnnotationType container : containers.get(at)) {
                typeManager.createContainmentDefinition(schema, at, container);
            }
        }
        for (AnnotationType at : data.keySet()) {
            for (PropertyType adt : data.get(at)) {
                typeManager.createPropertyDefinition(schema, at, adt);
            }
        }
    }

    @Override
    public Void onText(DTextPattern p) {
        if (!nestingStack.isEmpty()) {
            nestingStack.peek().setTextContainer(true);
        }
        return super.onText(p);
    }

    @Override
    public Void onElement(DElementPattern p) {
        Void result = null;
        for (javax.xml.namespace.QName xmlName : p.getName().listNames()) {
            AnnotationType annotationType = annotationTypeCache.get(xmlName);
            if (annotationType == null) {
                annotationTypeCache.put(xmlName, annotationType = typeManager.createType(name(xmlName), toString(p.getAnnotation())));
            }
            final AnnotationType container = nestingStack.peek();
            final Collection<AnnotationType> annotationContainers = containers.get(annotationType);
            if (container == null || !annotationContainers.contains(container)) {
                if (container != null) {
                    annotationContainers.add(container);
                }
                nestingStack.push(annotationType);
                result = super.onElement(p);
                nestingStack.pop();
            }
        }
        return result;
    }

    @Override
    public Void onRef(DRefPattern p) {
        final String target = p.getTarget().getName();
        if (deferenceStack.contains(target)) {
            return null;
        } else {
            deferenceStack.push(target);
            final Void result = super.onRef(p);
            deferenceStack.pop();
            return result;
        }
    }

    private Name name(javax.xml.namespace.QName xmlName) {
        Name name = nameCache.get(xmlName);
        if (name == null) {
            Namespace ns = namespaceCache.get(xmlName.getNamespaceURI());
            if (ns == null) {
                namespaceCache.put(xmlName.getNamespaceURI(), ns = nameManager.get(xmlName.getNamespaceURI()));
            }
            nameCache.put(xmlName, name = nameManager.create(ns, xmlName.getLocalPart()));
        }
        return name;
    }

    private String toString(DAnnotation annotation) {
        StringBuilder str = new StringBuilder();
        for (Element e : annotation.getChildren()) {
            str.append("\n").append(e.getTextContent());
        }
        return Strings.emptyToNull(str.toString().replaceAll("[\\p{Space}]+", " ").trim());
    }
}
