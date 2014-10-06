package de.tud.kom.stringmatching.test;

import de.tud.kom.stringmatching.shinglecloud.ShingleCloud;
import de.tud.kom.stringmatching.shinglecloud.ShingleCloudMatch;
import org.junit.Test;

/**
 * Simple examples on how to use ShingleCloud and GST.
 *  
 * @author Arno Mittelbach
 * @see ShingleCloud
 * @see de.tud.kom.stringmatching.GreedyStringTilingAlgorithm
 */
public class Examples {

	@Test
	public void shingleCloudExample(){
		String haystack = "This is some text that you want to search through";
		String needle = "some text you want to find";

		/* preparing the match object */
		ShingleCloud sc = new ShingleCloud(haystack);
		sc.setNGramSize(2);
		
		/* define that one matching shingle is enough to create a match object */
		sc.setMinimumNumberOfOnesInMatch(1);
		
		/* define that a match should be split as soon as one shingle does not match */
		sc.setMaximumNumberOfZerosBetweenMatches(0);
		
		/* do not sort the match objects by their rating but leave them in the order they occured */
		sc.setSortMatchesByRating(false);

		/* searching for the needle */
		sc.match(needle);

		/* displaying results */
		System.out.println("ShingleCloud found " + sc.getMatches().size() + " match(es).");

		ShingleCloudMatch firstMatch = sc.getMatches().get(0);
		System.out.println("The first match consists of " + firstMatch.getNumberOfMatchedShingles() + " shingle(s).");
		System.out.println("Its indirect rating is " + firstMatch.getContainmentInNeedle());
		System.out.println("The matching shingles were: " + firstMatch.getMatchedShingles());

		ShingleCloudMatch secondMatch = sc.getMatches().get(1);
		System.out.println("The second match consists of " + secondMatch.getNumberOfMatchedShingles() + " shingle(s).");
		System.out.println("Its indirect rating is " + secondMatch.getContainmentInNeedle());
		System.out.println("The matching shingles were: " + secondMatch.getMatchedShingles());
	}
}
