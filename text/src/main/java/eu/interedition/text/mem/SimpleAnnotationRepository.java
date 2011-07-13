package eu.interedition.text.mem;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.text.*;
import eu.interedition.text.util.AbstractAnnotationRepository;
import eu.interedition.text.util.Annotations;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import static com.google.common.collect.Iterables.*;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleAnnotationRepository extends AbstractAnnotationRepository {
  public Annotation create(Text text, QName name, Range range) {
    final SimpleAnnotation annotation = new SimpleAnnotation(text, name, range);
    ((SimpleText) text).add(annotation);
    return annotation;
  }

  public AnnotationLink createSet(QName name) {
    return new SimpleAnnotationLink(name);
  }

  public void delete(Annotation annotation) {
    for (AnnotationLink l : ((SimpleAnnotation) annotation).getLinks()) {
      ((SimpleAnnotationLink) l).remove(annotation);
    }
    ((SimpleText) ((SimpleAnnotation) annotation).getText()).remove(annotation);
  }

  public void delete(AnnotationLink annotationLink) {
    for (Annotation a : ((SimpleAnnotationLink) annotationLink)) {
      ((SimpleAnnotation) a).getLinks().remove(annotationLink);
    }
  }

  public void add(AnnotationLink to, Set<Annotation> toAdd) {
    for (Annotation a : toAdd) {
      ((SimpleAnnotation) a).getLinks().add(to);
    }
    ((SimpleAnnotationLink) to).addAll(toAdd);
  }

  public void remove(AnnotationLink from, Set<Annotation> toRemove) {
    for (Annotation a : toRemove) {
      ((SimpleAnnotation) a).getLinks().remove(from);
    }
    ((SimpleAnnotationLink) from).removeAll(toRemove);
  }

  public SortedSet<QName> names(Text text) {
    return Sets.newTreeSet(transform((SimpleText) text, Annotations.NAME));
  }

  public Iterable<Annotation> find(Text text, final Set<QName> names, final Set<Range> ranges) {
    Iterable<Annotation> annotations = (SimpleText) text;
    if (names != null && !names.isEmpty()) {
      annotations = filter(annotations, new Predicate<Annotation>() {
        public boolean apply(Annotation input) {
          return names.contains(input.getName());
        }
      });
    }
    if (ranges != null && !ranges.isEmpty()) {
      annotations = filter(annotations, new Predicate<Annotation>() {
        public boolean apply(final Annotation annotation) {
          return any(ranges, new Predicate<Range>() {
            public boolean apply(Range range) {
              return annotation.getRange().hasOverlapWith(range);
            }
          });
        }
      });
    }
    return annotations;
  }

  public Map<AnnotationLink, Set<Annotation>> findLinks(Set<Text> texts, final Set<QName> setNames, final Set<QName> names, final Set<Range> ranges) {
    Preconditions.checkArgument(texts != null && !texts.isEmpty());
    Iterable<Annotation> annotations = concat(transform(texts, new Function<Text, Iterable<Annotation>>() {

      public Iterable<Annotation> apply(Text input) {
        return find(input, names, ranges);
      }
    }));
    if (setNames != null && !setNames.isEmpty()) {
      annotations = filter(annotations, new Predicate<Annotation>() {
        public boolean apply(Annotation input) {
          return Iterables.any(((SimpleAnnotation) input).getLinks(), new Predicate<AnnotationLink>() {
            public boolean apply(AnnotationLink input) {
              return setNames.contains(input.getName());
            }
          });
        }
      });
    }

    final Map<AnnotationLink, Set<Annotation>> result = Maps.newHashMap();
    for (Annotation a : annotations) {
      for (AnnotationLink l : ((SimpleAnnotation)a).getLinks()) {
        Set<Annotation> linkedAnnotations = result.get(l);
        if (linkedAnnotations == null) {
          result.put(l, linkedAnnotations = Sets.newHashSet());
        }
        linkedAnnotations.add(a);
      }
    }

    return result;
  }
}
