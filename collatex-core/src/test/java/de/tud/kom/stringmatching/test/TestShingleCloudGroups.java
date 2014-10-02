package de.tud.kom.stringmatching.test;

import org.junit.Test;
import static org.junit.Assert.*;

import de.tud.kom.stringmatching.shinglecloud.ShingleCloud;
import de.tud.kom.stringmatching.shinglecloud.ShingleList;

/**
 * Very simple unit tests (should be expanded to a proper test suite).
 * 
 * @author amittelbach
 *
 */
public class TestShingleCloudGroups  {

	String needle = "This is the needle that we want to find but we still have much more text so that this should be split up into two matches";
	String haystack = "This is the needle that we want to find " +
			". And here is lot's of text that is not in the needle .. so what happens now?" +
			"but we still have much more text so that this should be split up into two matches";
	
	@Test
	public void testNoGroups(){
		
		ShingleCloud sc = new ShingleCloud(haystack);
		sc.setMaximumNumberOfZerosBetweenMatches(1);
		sc.setMinimumNumberOfOnesInMatch(3);
		sc.match(needle);
		
		assertEquals(2, sc.getMatches().size());
		System.out.println("indirectrating 1: " + sc.getMatches().get(0).getContainmentInNeedle());
		System.out.println("indirectrating 2: " + sc.getMatches().get(1).getContainmentInNeedle());
		System.out.println("rating 1: " + sc.getMatches().get(0).getRating());
		System.out.println("rating 2: " + sc.getMatches().get(1).getRating());
	}
	
	@Test
	public void testGroups(){
		ShingleList haystackList = new ShingleList();
		haystackList.addShingleGroup(haystack, "someGroup");
		ShingleCloud sc = new ShingleCloud(haystackList);
		sc.setDetectGroups(true);
		sc.setMaximumNumberOfZerosBetweenMatches(1);
		sc.setMinimumNumberOfOnesInMatch(3);
		sc.match(needle);
		
		assertEquals(1, sc.getMatches().size());
		System.out.println("indirectrating 1: " + sc.getMatches().get(0).getContainmentInNeedle());
		System.out.println("rating 1: " + sc.getMatches().get(0).getRating());
		System.out.println("grouprating 1: " + sc.getMatches().get(0).getGroupRating());
	}
}
