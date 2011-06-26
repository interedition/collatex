package eu.interedition.text.event;

import org.junit.Test;
import eu.interedition.text.AbstractXMLTest;
import eu.interedition.text.Annotation;
import org.springframework.beans.factory.annotation.Autowired;

public class EventGeneratorTest extends AbstractXMLTest {

	@Autowired
	private EventGenerator generator;
	
	@Test
	public void generateEvents() throws EventHandlerException {
		generator.generate(document("archimedes-palimpsest-tei.xml"), DEBUG_HANDLER);
	}

	private final EventHandler DEBUG_HANDLER = new EventHandler() {

		public void startAnnotation(Annotation annotation) {
			printDebugMessage("START: " + annotation);
		}

		public void endAnnotation(Annotation annotation) {
			printDebugMessage("END: " + annotation);
		}
	};
}
