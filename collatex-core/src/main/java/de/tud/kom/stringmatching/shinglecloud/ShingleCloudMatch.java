package de.tud.kom.stringmatching.shinglecloud;

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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.tud.kom.stringmatching.shinglecloud.Shingle.ShingleType;
import de.tud.kom.stringmatching.shinglecloud.ShingleCloud.ShingleCloudMarker;



/**
 * A wrapper object for a match with additional information.
 * 
 * <h2>Ratings</h2>
 * 
 * <h3>Rating</h3>
 * The basic rating is defined by the number of matched shingles divided by the matche's length.
 * 
 * <h3>Indirect Rating</h3>
 * The indirect rating is defined by the number of matching shingles divided by the number of shingles in the needle.
 * 
 * <h3>Group Rating</h3>
 * The group rating is defined by the number of matching shingles divided by the number of shingles in the surrounding group.
 * 
 * @author Arno Mittelbach
 * @see ShingleCloud
 */
public class ShingleCloudMatch implements Comparable<ShingleCloudMatch> {

	private int start;
	private int length;
	private ShingleList matchingShingles;
	
	private double rating = 0;
	private double indirectRating = 0;
	private double groupRating = 0;
	
	private Shingle upperBoundary;
	private Shingle lowerBoundary;
	private ShingleList skippedBoundaries;
	
	private int numberOfMatchedShingles;
	
	private int numberOfMatchedTokens;
	private int totalNumberOfTokensInMatch;
	
	private int numberOfMagicShingles;
	
	private int shingleSize;
	private double containmentInHaystack;
	
	/**
	 * 
	 * @param shingleCloud
	 * @param start
	 * @param matchLength
	 * @param haystackShingles
	 */
	public ShingleCloudMatch(List<ShingleCloudMarker> shingleCloud, int start, int matchLength, ShingleList haystackShingles, ShingleCloud sc) {
		/* store information as member variables */
		this.start = start;
		this.length = matchLength;
		shingleSize = haystackShingles.getNGramSize(); 
		totalNumberOfTokensInMatch = matchLength + shingleSize - 1;
		
		// find out which shingles we were matching
		numberOfMatchedShingles = 0;
		numberOfMatchedTokens = 0;
		
		boolean inMatch = false;
		
		matchingShingles = new ShingleList();
		for(int i = 0; i < length; i++){
			Shingle s = haystackShingles.get(i + start);
			if(s.getType() == ShingleType.Shingle)
				matchingShingles.addShingle(s);
			if(shingleCloud.get(i+start)==ShingleCloudMarker.Match){
				numberOfMatchedShingles++;
				if(inMatch)
					numberOfMatchedTokens++;
				else
					numberOfMatchedTokens += shingleSize;
				inMatch = true;
			} else if(shingleCloud.get(i+start)==ShingleCloudMarker.Magic){
				numberOfMagicShingles++;
				numberOfMatchedTokens++;
			} else
				inMatch = false;
		}
		
		// rating is number of matched shingles by length of match
		rating = numberOfMatchedTokens / (double) (length + shingleSize - 1);
		
		// Should we care about groups?
		if(sc.isDetectGroups()){
			
			// determine upper boundary
			for(int i = start; i >= 0; i--){
				if(shingleCloud.get(i) == ShingleCloudMarker.Group){
					Shingle s = haystackShingles.get(i);
					if(s.getType() == ShingleType.GroupStart){
						upperBoundary = s;
						break;
					}
				}
			}
			
			//determine lower boundary
			try{
				for(int i = start + length; i < shingleCloud.size(); i++){
					if(shingleCloud.get(i) == ShingleCloudMarker.Group){
						Shingle s = haystackShingles.get(i);
						if(s.getType() == ShingleType.GroupEnd){
							lowerBoundary = s;
							break;
						}
					}
				}
			} catch(IndexOutOfBoundsException e){}
			
			// determine skipped boundaries
			skippedBoundaries = new ShingleList();
			for(int i = start; i < start + length; i++)
			{
				try{
					if(shingleCloud.get(i) == ShingleCloudMarker.Group)
						skippedBoundaries.addShingle(haystackShingles.get(i));
				} catch(IndexOutOfBoundsException e){}
			}
			skippedBoundaries.compile();
		}
	}
	
	/**
	 * Creates a list of matches from the given parameters such that no match contains any skipped boundaries.
	 * 
	 * @param shingleCloud
	 * @param start
	 * @param matchLength
	 * @param haystackShingles
	 * @return
	 */
	public static List<ShingleCloudMatch> generateMatches(List<ShingleCloudMarker> shingleCloud, int start, int matchLength, ShingleList haystackShingles, ShingleCloud sc){
		List<ShingleCloudMatch> matches = new ArrayList<ShingleCloudMatch>();

		/* are we supposed to care about groups */
		if(! sc.isDetectGroups() || ! sc.isSplitUpMatchesOverGroups()){
			matches.add(new ShingleCloudMatch(shingleCloud, start, matchLength, haystackShingles, sc));
			return matches;
		}
		
		/* we are asked to split up the matches */
		int lastStart = start;
		boolean matched = false;
		for(int i = lastStart;i < start + matchLength; i++ ){
			matched = false;
			if(shingleCloud.get(i) == ShingleCloudMarker.Group){
				Shingle s = haystackShingles.get(i);
				if(s.getType() == ShingleType.GroupEnd){
					matches.add(new ShingleCloudMatch(shingleCloud, lastStart, i - lastStart, haystackShingles, sc));
					lastStart = i + 1;
					matched = true;
				}
			}
		}
		if(! matched){
			matches.add(new ShingleCloudMatch(shingleCloud, lastStart, start + matchLength - lastStart, haystackShingles, sc));
		}
		
		return matches;
	}
	
	public void combine(ShingleCloudMatch match){
		// update rating
		indirectRating += match.getContainmentInNeedle();
		rating = (rating + match.getRating()) / (double) 2;
		
		// update counters
		totalNumberOfTokensInMatch += match.getTotalNumberOfTokensInMatch();
		numberOfMatchedTokens += match.getNumberOfMatchedTokens();
		numberOfMagicShingles += match.getNumberOfMagicShingles();
		numberOfMatchedShingles += match.getNumberOfMatchedShingles();
		length += match.getLength();
		
		// update matched shingles
		addMatchedShingles(match.getMatchedShingles());
	}
	
	protected void addMatchedShingles(ShingleList list){
		for(int i = 0; i < list.size(); i++)
			matchingShingles.addShingle(list.get(i));
	}
	
	/**
	 * 
	 * @return The list of shingles that actually matched.
	 */
	public ShingleList getMatchedShingles(){
		if(! matchingShingles.isCompiled())
			matchingShingles.compile();
		return matchingShingles;
	}
		
	/**
	 * 
	 * @return Whether or not we have found a lower boundary
	 */
	public boolean hasUpperBound(){
		return null != upperBoundary;
	}
	
	/**
	 * 
	 * @return Whether or not we have found a lower boundary
	 */
	public boolean hasLowerBound(){
		return null != lowerBoundary;
	}
	
	/**
	 * 
	 * @return The upper bound or null
	 */
	public Shingle getUpperBound(){
		return upperBoundary;
	}
	
	/**
	 * 
	 * @return The lower bound or null
	 */
	public Shingle getLowerBound(){
		return lowerBoundary;
	}
	
	/**
	 * 
	 * @return The skipped bounds or null if group detection is disabled
	 * @see ShingleCloud#setDetectGroups(boolean)
	 */
	public ShingleList getSkippedBoundaries(){
		return skippedBoundaries;
	}

	/**
	 * The basic rating is defined by the number of matched tokens divided by the match's length (in tokens).
	 * 
	 * @return The matches rating.
	 * @see #getContainmentInNeedle()
	 * @see #getGroupRating()
	 */
	public double getRating() {
		return rating;
	}

	/**
	 * 
	 * @return The number of shingles in the match that actually matched.
	 */
	public int getNumberOfMatchedShingles() {
		return numberOfMatchedShingles;
	}
	
	/**
	 * 
	 * @return The number of tokens in the match that actually matched.
	 */
	public int getNumberOfMatchedTokens() {
		return numberOfMatchedTokens;
	}
	
	/**
	 * 
	 * @return Returns the total number of tokens in match .. whether actually matched or not
	 */
	public int getTotalNumberOfTokensInMatch(){
		return totalNumberOfTokensInMatch;
	}
	
	/**
	 * 
	 * @return The number of shingles in the match that were marked as "magic".
	 */
	public int getNumberOfMagicShingles() {
		return numberOfMagicShingles;
	}

	/**
	 * Returns the position in the shingle cloud where this match started.
	 * 
	 * @return the starting position in the shingle cloud.
	 */
	public int getStart(){
		return start;
	}
	
	public int getLength(){
		return length;
	}
	
	/**
	 * The indirect rating is defined by the number of matching tokens divided by the number of tokens in the needle.
	 * 
	 * @param indirectRating
	 */
	void setContainmentInNeedle(double indirectRating){
		this.indirectRating = indirectRating;
	}
	
	/**
	 * The indirect rating is defined by the number of matching tokens divided by the number of tokens in the needle.
	 * 
	 * @return The indirect rating.
	 * @see #getRating()
	 * @see #getGroupRating()
	 * @see #getContainmentInHaystack()
	 */
	public double getContainmentInNeedle() {
		return indirectRating;
	}
	
	/**
	 * The indirect rating is defined by the number of matching tokens divided by the number of tokens in the haystack.
	 * 
	 * @param containmentInHaystack
	 */
	void setContainmentInHaystack(double containmentInHaystack){
		this.containmentInHaystack = containmentInHaystack;
	}
	
	/**
	 * The indirect rating is defined by the number of matching tokens divided by the number of tokens in the haystack.
	 * 
	 * @return The indirect rating.
	 * @see #getRating()
	 * @see #getGroupRating()
	 * @see #getContainmentInHaystack()
	 */
	public double getContainmentInHaystack() {
		return containmentInHaystack;
	}

	/**
	 * Orders two matches by comparing their indirect ratings (and if equal their ratings).
	 */
	public int compareTo(ShingleCloudMatch o) {
		if( o.getContainmentInNeedle() == indirectRating ){
			if(rating == o.getRating())
				return 0;
			if(rating < o.getRating())
				return 1;
			return -1;
		} if( indirectRating < o.getContainmentInNeedle())
			return 1;
		
		return -1;
	}

	/**
	 * Set by the shingle cloud algorithm.
	 * 
	 * @param rating
	 */
	void setGroupRating(double rating) {
		this.groupRating  = rating;
	}

	/**
	 * The group rating is defined by the number of matching tokens divided by the number of tokens in the surrounding group.
	 * 
	 * @return The match's group rating.
	 * @see #getRating()
	 * @see #getContainmentInNeedle()
	 * @see ShingleCloud#setDetectGroups(boolean)
	 */
	public double getGroupRating(){
		return this.groupRating;
	}
}
