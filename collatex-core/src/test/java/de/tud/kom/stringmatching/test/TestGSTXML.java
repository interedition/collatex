package de.tud.kom.stringmatching.test;

import org.junit.Test;

import de.tud.kom.stringmatching.gst.GST;
import static org.junit.Assert.*;


public class TestGSTXML {

	@Test
	public void testGSTXML() throws Exception {
		String haystack = "Lorem ipsum <p>dolor sit amet, consectetur <strong style=\"lkalsdlasd askj;\">adipiscing elit Sed <i>placerat</i><br/> dui eget lorem</p> laoreet pharetra. Phasellus feugiat tristique libero, ut sagittis purus rhoncus quis.";
		String needle = "amet, consectetur adipiscing elit Sed placerat dui";

		GST gst = new GST(haystack);
		gst.useXMLMode();
		gst.setMinimumTileLength(3);
		gst.match(needle);
		
		assertEquals(1, gst.getTiles().size());
		assertEquals(7, gst.getTiles().get(0).getLength());
		assertEquals(1, gst.getContainmentInNeedle());
	}
	
	@Test
	public void testGSTBasic_multipleTiles() throws Exception {
		String haystack = "Lorem ipsum <p>dolor sit amet, consectetur <strong style=\"lkalsdlasd askj;\">adipiscing elit Sed <i>placerat</i><br/> dui eget lorem</p> laoreet pharetra. Phasellus feugiat tristique libero, ut sagittis purus rhoncus quis.";
		String needle = "amet, consectetur <strangemarker/> abcdkef elit<br/> Sed placerat dui";

		GST gst = new GST(haystack);
		gst.useXMLMode();
		gst.setMinimumTileLength(2);
		gst.match(needle);
		
		assertEquals(2, gst.getTiles().size());
		assertEquals(2, gst.getTiles().get(0).getLength());
		assertEquals(4, gst.getTiles().get(1).getLength());
		System.out.println(gst.getContainmentInNeedle());
		assertTrue(gst.getContainmentInNeedle() > 0.85 && gst.getContainmentInNeedle() < 0.86);
	}
}
