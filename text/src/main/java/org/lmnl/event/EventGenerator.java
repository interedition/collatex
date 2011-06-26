package org.lmnl.event;

import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import org.lmnl.AnnotationRepository;
import org.lmnl.Annotation;
import org.lmnl.Range;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.lmnl.Text;

public class EventGenerator {
	private Predicate<Annotation> filter = Predicates.alwaysTrue();
	private AnnotationRepository annotationRepository;

	public EventGenerator() {
	}

	public void setFilter(Predicate<Annotation> filter) {
		this.filter = filter;
	}

	public void setAnnotationRepository(AnnotationRepository annotationRepository) {
		this.annotationRepository = annotationRepository;
	}

	public void generate(Text text, EventHandler eventHandler) throws EventHandlerException {
		final SortedMap<Integer, List<Annotation>> opened = Maps.newTreeMap();
		for (Annotation annotation : Iterables.filter(annotationRepository.find(text), filter)) {
			final Range annotationRange = annotation.getRange();
			final int start = annotationRange.getStart();
			final int end = annotationRange.getEnd();

			for (Iterator<Integer> endingAtIt = opened.keySet().iterator(); endingAtIt.hasNext();) {
				final Integer endingAt = endingAtIt.next();
				if (endingAt > start) {
					break;
				}
				for (Annotation ending : opened.get(endingAt)) {
					eventHandler.endAnnotation(ending);
				}
				endingAtIt.remove();
			}

			eventHandler.startAnnotation(annotation);

			if (start == end) {
				eventHandler.endAnnotation(annotation);
			} else {
				List<Annotation> endingAnnotations = opened.get(end);
				if (endingAnnotations == null) {
					opened.put(end, endingAnnotations = Lists.newArrayList());
				}
				endingAnnotations.add(annotation);
			}
		}
		for (List<Annotation> remaining : opened.values()) {
			for (Annotation annotation : remaining) {
				eventHandler.endAnnotation(annotation);
			}
		}
	}
}
