package eu.interedition.text.mem;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.interedition.text.*;
import eu.interedition.text.util.Annotations;
import eu.interedition.text.util.SimpleAnnotationPredicate;

import java.util.*;

import static com.google.common.collect.Iterables.*;
import static eu.interedition.text.util.SimpleAnnotationPredicate.isNullOrEmpty;
import static java.util.Collections.singleton;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleAnnotationRepository implements AnnotationRepository {
  private Set<Annotation> annotations = Collections.synchronizedSet(new HashSet<Annotation>());

  public Annotation create(Text text, QName name, Range range) {
    final SimpleAnnotation annotation = new SimpleAnnotation(text, name, range);
    ((SimpleText) text).add(annotation);
    annotations.add(annotation);
    return annotation;
  }

  public AnnotationLink createLink(QName name) {
    return new SimpleAnnotationLink(name);
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

  public Iterable<Annotation> find(final AnnotationPredicate predicate) {
    Iterable<Annotation> annotations = Collections.unmodifiableSet(this.annotations);
    if (!isNullOrEmpty(predicate.annotationEquals())) {
      annotations = filter(annotations, new Predicate<Annotation>() {
        public boolean apply(Annotation input) {
          return predicate.annotationEquals().contains(input);
        }
      });
    }
    if (!isNullOrEmpty(predicate.annotationAnnotates())) {
      annotations = filter(annotations, new Predicate<Annotation>() {
        public boolean apply(Annotation input) {
          return predicate.annotationAnnotates().contains(((SimpleAnnotation) input).getText());
        }
      });
    }
    if (!isNullOrEmpty(predicate.annotationhasName())) {
      annotations = filter(annotations, new Predicate<Annotation>() {
        public boolean apply(Annotation input) {
          return predicate.annotationhasName().contains(input.getName());
        }
      });
    }
    if (!isNullOrEmpty(predicate.annotationOverlaps())) {
      annotations = filter(annotations, new Predicate<Annotation>() {
        public boolean apply(final Annotation annotation) {
          return any(predicate.annotationOverlaps(), new Predicate<Range>() {
            public boolean apply(Range range) {
              return annotation.getRange().hasOverlapWith(range);
            }
          });
        }
      });
    }
    return annotations;
  }

  public void delete(AnnotationPredicate predicate) {
    for (Annotation a : Sets.newHashSet(find(predicate))) {
      for (AnnotationLink l : ((SimpleAnnotation) a).getLinks()) {
        ((SimpleAnnotationLink) l).remove(a);
      }
      ((SimpleText) ((SimpleAnnotation) a).getText()).remove(a);
      annotations.remove(a);
    }
  }

  public Map<AnnotationLink, Set<Annotation>> findLinks(Set<Text> texts, final Set<QName> setNames, final Set<QName> names, final Map<Text, Set<Range>> ranges) {
    Preconditions.checkArgument(texts != null && !texts.isEmpty());
    Iterable<Annotation> annotations = concat(transform(texts, new Function<Text, Iterable<Annotation>>() {

      public Iterable<Annotation> apply(Text input) {
        return find(new SimpleAnnotationPredicate(null, singleton(input), names, ranges == null ? null : ranges.get(input)));
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
