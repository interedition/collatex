package org.lmnl;

import java.util.Collections;
import java.util.Set;

public abstract class AbstractAnnotationRepository implements AnnotationRepository {


	public Iterable<Annotation> find(Text text, Set<QName> names, Set<Range> ranges) {
		return find(text, names, ranges, true);
	}
	
	public Iterable<Annotation> find(Text text, Set<QName> names) {
		return find(text, names, null, true);
	}
	
	public Iterable<Annotation> find(Text text, QName name) {
		return find(text, Collections.singleton(name), null, true);
	}

	public Iterable<Annotation> find(Text text) {
		return find(text, null, null, true);
	}

}
