package de.tud.kom.stringmatching.gst;

/**
*
* This file is part of Shingle Cloud Library, Copyright (C) 2009 Arno Mittelbach, Lasse Lehmann
* 
* Shingle Cloud Library is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Shingle Cloud Library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with Shingle Cloud Library.  If not, see <http://www.gnu.org/licenses/>.
*
*
*/


import com.google.common.base.Function;
import com.google.common.base.Functions;
import de.tud.kom.stringmatching.shinglecloud.ShingleCloud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;




/**
 * Greedy String Tiling.
 * 
 * <p>
 * Implements the Greedy String Tiling algorithm as proposed by Michael J. Wise in his paper:
 * "String Similarity via Greedy String Tiling and Running Karp-Rabin Matching"
 * </p>
 * 
 * @author Arno Mittelbach
 *
 */
public class GST {

	private String haystack;
	private String needle;
	
	private String[] tokenizedHaystack;
	private String[] tokenizedNeedle; 
	private String[] preprocessedHaystack;

	private Function<String, String[]> tokenizer = ShingleCloud.WORD_TOKENIZER;
	private Function<String, String> preprocessingAlgorithm = Functions.identity();

	private boolean preprocessFirst = false;
	private boolean ignoreMode = false;
	private int minimumTileLength = 3;
	
	private List<GSTTile> tiles;
	private boolean compiled = false;
	
	
	/**
	 * Create a new GST object with the specified haystack.
	 * 
	 * @param haystack
	 */
	public GST(String haystack){
		this.haystack = haystack;
	}

	/**
	 * Prepares the object for match operations.
	 * 
	 * <p>The haystack is preprocessed and tokenized. The object needs to be compiled
	 * before needles can be matched against it.</p>
	 * 
	 * @see #match(String)
	 */
	public void compile(){
		if(isCompiled())
			throw new IllegalStateException();

		if(isPreprocessFirst())
			haystack = preprocessingAlgorithm.apply(haystack);
		
		if(!ignoreMode){
			tokenizedHaystack = tokenizer.apply(haystack);
			haystack = preprocessingAlgorithm.apply(haystack);
			preprocessedHaystack = new String[tokenizedHaystack.length];
	        for(int i = 0; i < tokenizedHaystack.length; i++)
	        	preprocessedHaystack[i] = preprocessingAlgorithm.apply(tokenizedHaystack[i]);
	        
		} else {
			tokenizedHaystack = tokenizer.apply(haystack);
			
			preprocessedHaystack = new String[tokenizedHaystack.length];
			for(int i = 0; i < tokenizedHaystack.length; i++)
				if(tokenizedHaystack[i].charAt(0) == '<')
					preprocessedHaystack[i] = tokenizedHaystack[i];
				else
					preprocessedHaystack[i] = preprocessingAlgorithm.apply(tokenizedHaystack[i]);
		}
		
		// set flag
		compiled = true;
	}
	
	
	/**
	 * 
	 * @param needle
	 */
	public void match(String needle){
		// test if object is already compiled .. and if not compile it
		if(! isCompiled())
			compile();
		
		/* store needle */
		this.needle = needle;
		
		if(ignoreMode)
			matchIgnoringXML(needle);
		else
			matchNormally(needle);
	}
	
	private void matchIgnoringXML(String needle) {
		if(isPreprocessFirst())
			needle = preprocessingAlgorithm.apply(needle);
		
		// Extract Tokens
        tokenizedNeedle = tokenizer.apply(needle);
        
        // Create list of preprocessed tokens
        String[] preprocessedNeedle = new String[tokenizedNeedle.length];
        for(int i = 0; i < preprocessedNeedle.length; i++)
        	if(tokenizedNeedle[i].charAt(0) == '<')
        		preprocessedNeedle[i] = tokenizedNeedle[i];
        	else
        		preprocessedNeedle[i] = preprocessingAlgorithm.apply(tokenizedNeedle[i]);

        // unmark all tokens
        boolean[] markedNeedle = new boolean[tokenizedNeedle.length];
        for (int i = 0; i < markedNeedle.length; i++)
            markedNeedle[i] = false;

        boolean[] markedHaystack = new boolean[tokenizedHaystack.length];
        for (int i = 0; i < tokenizedHaystack.length; i++)
            markedHaystack[i] = false;

        
        // prepare list of tiles
        List<GSTTile> tmpTiles = new ArrayList<GSTTile>();
        
        int maxmatch = 0;
        int lengthTiled = 0;
        HashMap<Integer, List<Integer[]>> matches = new HashMap<Integer, List<Integer[]>>();

        do{
            maxmatch = minimumTileLength;
            for (int i = 0; i < tokenizedNeedle.length; i++) {
                // move on, if we are a tag
            	if(tokenizedNeedle[i].charAt(0) == '<')
                	continue;
                
            	for (int j = 0; j < tokenizedHaystack.length; j++) {
            		// move on, if we are a tag
                	if(tokenizedHaystack[j].charAt(0) == '<')
                    	continue;

                	int m = 0;
                	int needleOffset = 0;
                	int haystackOffset = 0;
                    for (int k = 0;
                        k + j + haystackOffset < tokenizedHaystack.length && 
                        k + i + needleOffset < tokenizedNeedle.length && 
                        !markedNeedle[i + k + needleOffset] && !markedHaystack[j + k + haystackOffset];
                        k++) {
                    	  if(tokenizedHaystack[j + k + haystackOffset].charAt(0) == '<'){
                    		  haystackOffset++;
                    		  k--;
                    		  continue;
                    	  }
                    	  if(tokenizedNeedle[i + k + needleOffset].charAt(0) == '<'){
                    		  needleOffset++;
                    		  k--;
                    		  continue;
                    	  }
                    	  if(!preprocessedHaystack[j + k + haystackOffset].equals(preprocessedNeedle[i + k + needleOffset]))
                    	      	break;
	                      m++;
                    }

                    if (m >= maxmatch) {
                        if (!matches.containsKey(m))
                            matches.put(m, new ArrayList<Integer[]>());

                        List<Integer[]> theMatches = matches.get(m);
                        theMatches.add(new Integer[] { i, j });
                    }

                    if (m > maxmatch) {
                        maxmatch = m;
                    }
                }
            }

            
            if (matches.containsKey(maxmatch)) {
            	List<Integer[]> allMatches = matches.get(maxmatch);

                for (Integer[] match : allMatches) {
                    boolean occluded = false;

                    int needleOffset = 0;
                	int haystackOffset = 0;
                    for (int j = 0; j < maxmatch; j++) {
                    	 if(tokenizedHaystack[match[1] + j + haystackOffset].charAt(0) == '<'){
                   		  haystackOffset++;
	                   		  j--;
	                   		  continue;
	                   	  }
	                   	  if(tokenizedNeedle[match[0] + j + needleOffset].charAt(0) == '<'){
	                   		  needleOffset++;
	                   		  j--;
	                   		  continue;
	                   	  }
                    	  if (markedNeedle[match[0] + j + needleOffset] || markedHaystack[match[1] + j + haystackOffset]) {
                              occluded = true;
                              break;
                          }
                    }

                    if (!occluded) {
                        String tile = "";
                        needleOffset = 0;
                    	haystackOffset = 0;
                        for (int j = 0; j < maxmatch; j++) {
                        	 if(tokenizedHaystack[match[1] + j + haystackOffset].charAt(0) == '<'){
                        		  tile += tokenizedHaystack[match[1] + haystackOffset + j] + " ";
                        		  haystackOffset++;
		                   		  j--;
		                   		  continue;
		                   	  }
		                   	  if(tokenizedNeedle[match[0] + j + needleOffset].charAt(0) == '<'){
		                   		  needleOffset++;
		                   		  j--;
		                   		  continue;
		                   	  }
                        	markedNeedle[match[0] + needleOffset + j] = true;
                            markedHaystack[match[1] + haystackOffset + j] = true;
                            tile += tokenizedHaystack[match[1] + haystackOffset + j] + " ";
                        }

                        tmpTiles.add(new GSTTile(match[1], maxmatch, tile.substring(0, tile.length() - 1)));

                        lengthTiled += maxmatch;
                    }
                }
            }

        } while (maxmatch > minimumTileLength);

        /* store tiles and sort them */
        postProcessTiles(tmpTiles);
	}

	private void matchNormally(String needle){
		if(isPreprocessFirst())
			needle = preprocessingAlgorithm.apply(needle);
		
		// Extract Tokens
        tokenizedNeedle = tokenizer.apply(needle);
        
        // Create list of preprocessed tokens
        String[] preprocessedNeedle = new String[tokenizedNeedle.length];
        for(int i = 0; i < tokenizedNeedle.length; i++)
        	preprocessedNeedle[i] = preprocessingAlgorithm.apply(tokenizedNeedle[i]);

        // unmark all tokens
        boolean[] markedNeedle = new boolean[tokenizedNeedle.length];
        for (int i = 0; i < markedNeedle.length; i++)
            markedNeedle[i] = false;

        boolean[] markedHaystack = new boolean[tokenizedHaystack.length];
        for (int i = 0; i < tokenizedHaystack.length; i++)
            markedHaystack[i] = false;

        
        // prepare list of tiles
        List<GSTTile> tmpTiles = new ArrayList<GSTTile>();
        
        int maxmatch = 0;
        int lengthTiled = 0;
        HashMap<Integer, List<Integer[]>> matches = new HashMap<Integer, List<Integer[]>>();

        do{
            maxmatch = minimumTileLength;
            for (int i = 0; i < tokenizedNeedle.length; i++) {
                for (int j = 0; j < tokenizedHaystack.length; j++) {
                    int m = 0;
                    for (int k = 0;
                        k + j < tokenizedHaystack.length && k + i < tokenizedNeedle.length
                        && !markedNeedle[i + k] && !markedHaystack[j + k] && 
                        preprocessedHaystack[j + k].equals(preprocessedNeedle[i + k]);  // preprocess and test on equality 
                        k++) {
                    		m++;
                    }

                    if (m >= maxmatch) {
                        if (!matches.containsKey(m))
                            matches.put(m, new ArrayList<Integer[]>());

                        List<Integer[]> theMatches = matches.get(m);
                        theMatches.add(new Integer[] { i, j });
                    }

                    if (m > maxmatch) {
                        maxmatch = m;
                    }
                }
            }

            
            if (matches.containsKey(maxmatch)) {
            	List<Integer[]> allMatches = matches.get(maxmatch);

                for (Integer[] match : allMatches) {
                    boolean occluded = false;

                    for (int j = 0; j < maxmatch; j++) {
                        if (markedNeedle[match[0] + j] || markedHaystack[match[1] + j]) {
                            occluded = true;
                            break;
                        }
                    }

                    if (!occluded) {
                        String tile = "";
                        for (int j = 0; j < maxmatch; j++) {
                            markedNeedle[match[0] + j] = true;
                            markedHaystack[match[1] + j] = true;
                            tile += tokenizedHaystack[match[1] + j] + " ";
                        }

                        tmpTiles.add(new GSTTile(match[1], maxmatch, tile.substring(0, tile.length() - 1)));

                        lengthTiled += maxmatch;
                    }
                }
            }

        } while (maxmatch > minimumTileLength);
        
       /* store and sort tiles */
       postProcessTiles(tmpTiles);
	}
	
	protected void postProcessTiles(List<GSTTile> tmpTiles) {
       tiles = new ArrayList<GSTTile>();
       while(tmpTiles.size() > 0){
	        // find smallest
	        GSTTile nextTile = tmpTiles.get(0);
	        int start = nextTile.getStart();
	        for(int i = 1; i < tmpTiles.size(); i++){
	        	GSTTile current = tmpTiles.get(i);
	        	if(current.getStart() < start){
	        		nextTile = current;
	        		start = current.getStart();
	        	}
	        }
	        
	        // move it
	        tiles.add(nextTile);
	        
	        // remove it
	        tmpTiles.remove(nextTile);
       }
	}
	
	
	/**
	 * 
	 * @return Whether or not this object has already been compiled
	 */
	public boolean isCompiled(){
		return compiled;
	}
	
	/**
	 * 
	 * @return The tokenizer used to tokenize haystack and needle.
	 */
	public Function<String, String[]> getTokenizer() {
		return tokenizer;
	}

	/**
	 * Sets the tokenizer used to tokenize needle and haystack.
	 * 
	 * <p>Cannot be set after object was compiled.</p>
	 * 
	 * @param tokenizer
	 * @see #compile()
	 */
	public void setTokenizer(Function<String, String[]> tokenizer) {
		if(isCompiled())
			throw new IllegalStateException();
		
		this.tokenizer = tokenizer;
	}

	/**
	 * 
	 * @return The minimum tile length needed for a match.
	 */
	public int getMinimumTileLength() {
		return minimumTileLength;
	}

	/**
	 * Sets the minimum tile length needed for a match.
	 * @param minimumTileLength
	 */
	public void setMinimumTileLength(int minimumTileLength) {
		this.minimumTileLength = minimumTileLength;
	}
	
	/**
	 * 
	 * @return The preprocessing algorithm used for preprocesseing needle and haystack.
	 */
	public Function<String, String> getPreprocessingAlgorithm() {
		return preprocessingAlgorithm;
	}

	/**
	 * Sets the preprocessing algorithm used for preprocesseing needle and haystack.
	 * 
	 * <p>Cannot be set after object was compiled.</p>
	 * 
	 * @param preprocessingAlgorithm
	 * @see #compile()
	 */
	public void setPreprocessingAlgorithm(Function<String, String> preprocessingAlgorithm) {
		if(isCompiled())
			throw new IllegalStateException();
		
		this.preprocessingAlgorithm = preprocessingAlgorithm;
	}

	/**
	 * 
	 * @return Whether matching is done in XML mode.
	 */
	public boolean isXMLMode() {
		return ignoreMode;
	}

	/**
	 * 
	 * @return The tokenized needle (the last one that was searched for).
	 * @see #match(String)
	 */
	public String[] getTokenizedNeedle() {
		return tokenizedNeedle;
	}
	
	
	/**
	 * 
	 * @return The tokenized haystack.
	 */
	public String[] getTokenizedHaystack() {
		return tokenizedHaystack;
	}

	/**
	 * 
	 * @return The list of tiles generated by the last match
	 */
	public List<GSTTile> getTiles() {
		return tiles;
	}
	
	/**
	 * 
	 * @return The haystack.
	 */
	public String getHaystack(){
		return haystack;
	}
	
	/**
	 * 
	 * @return The number of tokens in the haystack.
	 */
	public int getHaystackLength(){
		if(! isCompiled())
			throw new IllegalStateException("Object has not yet been compiled.");
		return tokenizedHaystack.length;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getNeedleLength(){
		if(null == tokenizedNeedle)
			throw new IllegalStateException("So far no match operation was performed.");
		return tokenizedNeedle.length;
	}
	
	/**
	 * 
	 * @return The length of all matched tiles combined divided by the number of tokens in the haystack.
	 */
	public double getContainmentInHaystack(){
		if(null == getTiles())
			throw new IllegalStateException("So far no match operation was performed.");
		
		int tileLength = 0;
		for(GSTTile tile : getTiles())
			tileLength += tile.getLength();
		return tileLength / (double)getHaystackLength();
	}

	/**
	 * 
	 * @return The length of all matched tiles combined divided by the number of tokens in the needle. 
	 */
	public double getContainmentInNeedle(){
		if(null == getTiles())
			throw new IllegalStateException("So far no match operation was performed.");
		
		int tileLength = 0;
		for(GSTTile tile : getTiles())
			tileLength += tile.getLength();
		return (double) tileLength / (double)getNeedleLength();
	}
	
	public boolean isPreprocessFirst() {
		return preprocessFirst;
	}

	public void setPreprocessFirst(boolean preprocessFirst) {
		this.preprocessFirst = preprocessFirst;
	}
	
	
}
