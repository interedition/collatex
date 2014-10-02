package de.tud.kom.stringmatching.test;

import de.tud.kom.stringmatching.gst.GST;
import de.tud.kom.stringmatching.gst.GSTTile;
import de.tud.kom.stringmatching.shinglecloud.ShingleCloud;
import de.tud.kom.stringmatching.shinglecloud.ShingleCloudMatch;
import org.junit.Test;

/**
 * Simple examples on how to use ShingleCloud and GST.
 *  
 * @author Arno Mittelbach
 * @see ShingleCloud
 * @see GST
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
	
	@Test
	public void gstExample(){
		String haystack = "This is some text that you want to search through";
		String needle = "some text you want to find";

		/* using the shingle cloud */
		GST gst = new GST(haystack);
		gst.setMinimumTileLength(2);
		gst.match(needle);
		
		System.out.println("GST found " + gst.getTiles().size() + " tile(es).");
		System.out.println("Containment in the needle " + gst.getContainmentInNeedle());
		System.out.println("Containment in the haystack " + gst.getContainmentInHaystack());
		
		GSTTile firstTile = gst.getTiles().get(0);
		System.out.println("The first tile consists of " + firstTile.getLength() + " tokens.");
		System.out.println("The matching tokens were: " + firstTile.getText());
		
		GSTTile secondTile = gst.getTiles().get(1);
		System.out.println("The second tile consists of " + secondTile.getLength() + " tokens.");
		System.out.println("The matching tokens were: " + secondTile.getText());
	}
}
