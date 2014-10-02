package de.tud.kom.stringmatching.test;

import org.junit.Test;
import static org.junit.Assert.*;

import de.tud.kom.stringmatching.shinglecloud.ShingleCloud;

public class TestMagicMatchers {

	@Test
	public void simpleTest(){
		String haystack = "Lorem ipsum dolor sit amet, consectetur adipiscing MAGIC Sed placerat dui eget lorem laoreet pharetra. Phasellus feugiat tristique libero, ut sagittis purus rhoncus quis.";
		String needle = "amet, consectetur adipiscing elit Sed placerat dui";
		
		ShingleCloud sc = new ShingleCloud(haystack);
		sc.setMagicWords(new String[]{"MAGIC"});
		sc.setNGramSize(3);
		sc.setMinimumNumberOfOnesInMatch(3);
		sc.setMaximumNumberOfZerosBetweenMatches(1);
		sc.match(needle);
		
		assertEquals(1, sc.getMatches().size());
		assertEquals(3, sc.getMatches().get(0).getNumberOfMagicShingles());
		assertEquals(2, sc.getMatches().get(0).getNumberOfMatchedShingles());
		
		sc.setMagicToOneFactor(3);
		sc.match(needle);
		
		assertEquals(1, sc.getMatches().size());
		assertEquals(3, sc.getMatches().get(0).getNumberOfMagicShingles());
		assertEquals(2, sc.getMatches().get(0).getNumberOfMatchedShingles());
		
		sc.setMagicToOneFactor(4);
		sc.match(needle);
		
		assertEquals(0, sc.getMatches().size());

	}
}
