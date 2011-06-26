package org.lmnl.event;

import org.lmnl.Annotation;


public interface EventHandler {
	void startAnnotation(Annotation annotation) throws EventHandlerException;

	void endAnnotation(Annotation annotation) throws EventHandlerException;
}
