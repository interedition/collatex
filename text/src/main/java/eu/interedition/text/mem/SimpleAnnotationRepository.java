package eu.interedition.text.mem;

import com.google.common.collect.Sets;
import eu.interedition.text.*;
import eu.interedition.text.predicate.*;
import eu.interedition.text.predicate.Predicate;
import eu.interedition.text.util.AbstractAnnotationRepository;
import eu.interedition.text.util.Annotations;

import java.util.*;

import static com.google.common.collect.Iterables.*;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class SimpleAnnotationRepository extends AbstractAnnotationRepository {
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

  public void deleteLinks(Iterable<Predicate> predicates) {
    for (AnnotationLink annotationLink : findLinks(predicates).keySet()) {
      for (Annotation a : ((SimpleAnnotationLink) annotationLink)) {
        ((SimpleAnnotation) a).getLinks().remove(annotationLink);
      }
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

  public Iterable<Annotation> find(final Iterable<AnnotationPredicate> predicates) {
    Iterable<Annotation> annotations = Collections.unmodifiableSet(this.annotations);

    final Set<Annotation> equals = Sets.newHashSet(transform(filter(predicates, AnnotationIdentityPredicate.class), AnnotationIdentityPredicate.TO_ANNOTATION));
    if (!equals.isEmpty()) {
      annotations = filter(annotations, new com.google.common.base.Predicate<Annotation>() {
        public boolean apply(Annotation input) {
          return equals.contains(input);
        }
      });
    }
    final Set<Text> texts = Sets.newHashSet(transform(filter(predicates, TextPredicate.class), TextPredicate.TO_TEXT));
    if (!texts.isEmpty()) {
      annotations = filter(annotations, new com.google.common.base.Predicate<Annotation>() {
        public boolean apply(Annotation input) {
          return texts.contains(((SimpleAnnotation) input).getText());
        }
      });
    }

    final Set<QName> names = Sets.newHashSet(transform(filter(predicates, AnnotationNamePredicate.class), AnnotationNamePredicate.TO_NAME));
    if (!names.isEmpty()) {
      annotations = filter(annotations, new com.google.common.base.Predicate<Annotation>() {
        public boolean apply(Annotation input) {
          return names.contains(input.getName());
        }
      });
    }

    final Iterable<TextRangePredicate> ranges = filter(predicates, TextRangePredicate.class);
    if (!isEmpty(ranges)) {
      annotations = filter(annotations, new com.google.common.base.Predicate<Annotation>() {
        public boolean apply(final Annotation annotation) {
          return any(ranges, new com.google.common.base.Predicate<TextRangePredicate>() {
            public boolean apply(TextRangePredicate p) {
              final Text text = ((SimpleAnnotation) annotation).getText();
              return text.equals(p.getText()) && annotation.getRange().hasOverlapWith(p.getRange());
            }
          });
        }
      });
    }
    return annotations;
  }

  public void delete(Iterable<AnnotationPredicate> predicate) {
    for (Annotation a : Sets.newHashSet(find(predicate))) {
      for (AnnotationLink l : ((SimpleAnnotation) a).getLinks()) {
        ((SimpleAnnotationLink) l).remove(a);
      }
      ((SimpleText) ((SimpleAnnotation) a).getText()).remove(a);
      annotations.remove(a);
    }
  }

  public Map<AnnotationLink, Set<Annotation>> findLinks(Iterable<Predicate> predicates) {
    /*
    Preconditions.checkArgument(texts != null && !texts.isEmpty());
    Iterable<Annotation> annotations = concat(transform(texts, new Function<Text, Iterable<Annotation>>() {

      public Iterable<Annotation> apply(Text input) {
        return find(new AnnotatedText(input), names, ranges == null ? null : ranges.get(input)));
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
      for (AnnotationLink l : ((SimpleAnnotation) a).getLinks()) {
        Set<Annotation> linkedAnnotations = result.get(l);
        if (linkedAnnotations == null) {
          result.put(l, linkedAnnotations = Sets.newHashSet());
        }
        linkedAnnotations.add(a);
      }
    }

    return result;
    */
    throw new UnsupportedOperationException();
  }
}
