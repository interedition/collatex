package de.tud.kom.stringmatching.test;

import org.junit.Test;

import de.tud.kom.stringmatching.gst.GST;
import static org.junit.Assert.*;


public class TestGSTBasic {

	@Test
	public void testGSTBasic() throws Exception {
		String haystack = "Lorem ipsum dolor sit amet, consectetur adipiscing elit Sed placerat dui eget lorem laoreet pharetra. Phasellus feugiat tristique libero, ut sagittis purus rhoncus quis.";
		String needle = "amet, consectetur adipiscing elit Sed placerat dui";

		GST gst = new GST(haystack);
		gst.setMinimumTileLength(3);
		gst.match(needle);
		
		assertEquals(1, gst.getTiles().size());
		assertEquals(7, gst.getTiles().get(0).getLength());
		assertEquals(1, gst.getContainmentInNeedle(), 0);
	}
	
	@Test
	public void testGSTBasic_multipleTiles() throws Exception {
		String haystack = "Lorem ipsum dolor sit amet, consectetur adipiscing elit Sed placerat dui eget lorem laoreet pharetra. Phasellus feugiat tristique libero, ut sagittis purus rhoncus quis.";
		String needle = "amet, consectetur abcdkef elit Sed placerat dui";

		GST gst = new GST(haystack);
		gst.setMinimumTileLength(2);
		gst.match(needle);
		
		assertEquals(2, gst.getTiles().size());
		assertEquals(2, gst.getTiles().get(0).getLength());
		assertEquals(4, gst.getTiles().get(1).getLength());
		assertTrue(gst.getContainmentInNeedle() > 0.85 && gst.getContainmentInNeedle() < 0.86);
	}
}
