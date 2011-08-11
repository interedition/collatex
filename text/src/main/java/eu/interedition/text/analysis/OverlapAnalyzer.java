package eu.interedition.text.analysis;

import com.google.common.collect.Sets;
import eu.interedition.text.Annotation;
import eu.interedition.text.QName;
import eu.interedition.text.event.AnnotationEventAdapter;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class OverlapAnalyzer extends AnnotationEventAdapter {
  protected Set<QName> selfOverlapping;

  protected Set<SortedSet<QName>> overlapping;

  protected Set<Annotation> started;

  public Set<QName> getSelfOverlapping() {
    return selfOverlapping;
  }

  public Set<SortedSet<QName>> getOverlapping() {
    return overlapping;
  }

  @Override
  public void start() {
    selfOverlapping = Sets.newHashSet();
    overlapping = Sets.newHashSet();
    started = Sets.newHashSet();
  }

  @Override
  public void start(long offset, Map<Annotation, Map<QName, String>> annotations) {
    started.addAll(annotations.keySet());
  }

  @Override
  public void end(long offset, Map<Annotation, Map<QName, String>> annotations) {
    final Set<Annotation> endings = annotations.keySet();
    started.removeAll(endings);

    for (Annotation ending : endings) {
      final QName endingName = ending.getName();
      for (Annotation started : this.started) {
        final QName startedName = started.getName();
        if (!started.getRange().encloses(ending.getRange())) {
          if (startedName.equals(endingName)) {
            selfOverlapping.add(endingName);
          } else {
            overlapping.add(Sets.newTreeSet(Sets.newHashSet(startedName, endingName)));
          }
        }
      }
    }
  }
}
