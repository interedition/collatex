package de.tud.kom.stringmatching.test;

import org.junit.Test;

import de.tud.kom.stringmatching.lcsubsequence.LCSResult;
import de.tud.kom.stringmatching.lcsubsequence.LongestCommonSubsequence;

public class LCSTest {

	@Test
	public void testLCS(){
		String test2 = "computer";
		String test1 = "boathouser";
		
		LongestCommonSubsequence lcs = new LongestCommonSubsequence();
		lcs.setBacktrace(true);
		LCSResult result = lcs.lcs(test1, test2);
		
		//MatrixUtils.printArray(result.getFMatrix(), 5);
		result.printResult();
	}
}
