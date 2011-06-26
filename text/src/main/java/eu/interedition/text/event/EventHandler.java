package eu.interedition.text.event;

import eu.interedition.text.Annotation;


public interface EventHandler {
	void startAnnotation(Annotation annotation) throws EventHandlerException;

	void endAnnotation(Annotation annotation) throws EventHandlerException;
}
