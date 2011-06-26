package org.lmnl;

import java.util.Set;

public interface AnnotationRepository {

	Iterable<Annotation> find(Text text, Set<QName> names, Set<Range> ranges, boolean overlapping);
	
	Iterable<Annotation> find(Text text, Set<QName> names, Set<Range> ranges);
	
	Iterable<Annotation> find(Text text, Set<QName> names);
	
	Iterable<Annotation> find(Text text, QName name);

	Iterable<Annotation> find(Text text);

}
