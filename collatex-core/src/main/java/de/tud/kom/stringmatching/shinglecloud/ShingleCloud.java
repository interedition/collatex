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


import com.google.common.base.Function;
import com.google.common.base.Functions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;



/**
 * Implements the ShingleCloud algorithm.
 * 
 * @author Arno Mittelbach
 *
 */
public class ShingleCloud {

	/**
	 * Stores the haystack this object is working on.
	 */
	protected String haystack;
	
	/**
	 * Stores the shingles for the haystack.
	 */
	protected ShingleList haystackShingles;
	
	/**
	 * Stores the n-gram size we use.
	 */
	protected int nGramSize = 3;
	
	/**
	 * Stores the minimum number of 1s in a match.
	 */
	protected int minimumNumberOfOnesInMatch = 3;
	
	/**
	 * Stores the minimum number of 0s between two matches.
	 */
	protected int maximumNumberOfZerosBetweenMatches = 1;
	
	/**
	 * Defines the number of magic tokens that need to occur to denote another one. 
	 */
	protected int magicToOneFactor = 1;
	
	/**
	 * Parameter that tells shingle cloud to combine multiple matches that have occured  in a group to one match.
	 */
	protected boolean combineMatchesInGroups = false;
	
	/**
	 * The preprocessing algorithm.
	 */
	protected Function<String, String> preprocessingAlgorithm = Functions.identity();
	
	/**
	 * Parameter that tells shingle cloud to use grouping
	 */
	protected boolean detectGroups = false;
	
	/**
	 * Parameter that tells shingle cloud to split up matches, such that each match falls into exactly one group.
	 */
	protected boolean splitUpMatchesOverGroups = true;
	
	/**
	 * Stores the last used shingle cloud
	 */
	private List<ShingleCloudMarker> shingleCloud;
	
	/**
	 * Stores the last matches
	 */
	private List<ShingleCloudMatch> matches;
	
	/**
	 * Stores the shingle list for the last matche's needle 
	 */
	private ShingleList needleShingles;

  public static final Function<String, String[]> WORD_TOKENIZER = new Function<String, String[]>() {
    @Nullable
    @Override
    public String[] apply(@Nullable String input) {
      return input.split("\\s+");
    }
  };
  private Function<String, String[]> tokenizer = WORD_TOKENIZER;
	
	/**
	 * Stores the length of each group (in shingles)
	 */
	private Map<String, Integer> groupLengthMap = new HashMap<String, Integer>();
	
	/**
	 * Tells whether or not this object has already been compiled.
	 */
	protected boolean compiled = false;
	
	protected boolean allowMultipleNeedleMatches = false;
	protected boolean allowNonIsolatedMultipleNeedleMatches = true;
	
	protected String[] magicWords;
	
	protected boolean sortMatchesByRating = true;

	private boolean adjustNeighboringMagic = true;
	
	/**
	 * Defines markers that are used in the shingle cloud instead of numbers.
	 * 
	 * A 0 represents a noMatch, a 1 stands for a match a 2 for a group marker
	 * and a 3 for a magic shingle.
	 * 
	 * @author amittelbach
	 *
	 */
	public enum ShingleCloudMarker {
		NoMatch,
		Match,
		Group,
		Magic,
		OutOfMatches
	}
	
	/**
	 * Implicit constructor for subclasses.
	 */
	protected ShingleCloud(){
		
	}
	
	/**
	 * Creates a new shingle cloud object that can be used to search inside the specified haystack.
	 * 
	 * @param haystack The haystack that is to be searched.
	 */
	public ShingleCloud(String haystack) {
		this.haystack = haystack;
	}
	
	/**
	 * Directly compiles the object with the properties used in the corresponding shingle list.
	 * 
	 * @param haystackShingles
	 */
	public ShingleCloud(ShingleList haystackShingles) {
		this.haystackShingles = haystackShingles;
		this.nGramSize = haystackShingles.getNGramSize();
		this.preprocessingAlgorithm = haystackShingles.getPreprocessingAlgorithm();
		this.tokenizer = haystackShingles.getTokenizer();
		this.haystackShingles.compile();
		compiled = true;
	}
	
	/**
	 * This function compiles the object and has to be called before the first match operation.
	 * 
	 * <p>
	 * What is done is that it creates a shingle set for the haystack.
	 * </p>
	 */
	public void compile() {
		if(isCompiled())
			throw new IllegalStateException("Object has already been compiled.");
		
		// prepare haystack shingle object
		haystackShingles = new ShingleList();
		haystackShingles.setNGramSize(nGramSize);
		haystackShingles.setPreprocessingAlgorithm(preprocessingAlgorithm);
		haystackShingles.setTokenizer(tokenizer);
		if(null != magicWords)
			haystackShingles.setMagicWords(magicWords);
		
		// add haystack to it
		haystackShingles.addTextToShingle(haystack);
		
		// compile object
		haystackShingles.compile();
		
		// Set compiled flag to true
		compiled = true;
	}
	
	/**
	 * 
	 * @return Whether or not this object has already been compiled.
	 */
	public boolean isCompiled()	{
		return compiled;
	}
	
	
	/**
	 * Performs a match operation on the specified needle.
	 * 
	 * <p>
	 * Before any match operation can be performed the object needs to be compiled. If you have not
	 * called compile yet, we will do this for you, but be aware, that you cannot change certain
	 * properties afterwards anymore.
	 * </p>
	 * 
	 * @param needle The needle to be searched for.
	 * @see compile
	 */
	public void match(String needle) {
		// test if object is already compiled .. and if not compile it
		if(! isCompiled())
			compile();
		
		// prepare needle
		needleShingles = new ShingleList();
		needleShingles.setNGramSize(nGramSize);
		needleShingles.setPreprocessingAlgorithm(preprocessingAlgorithm);
		needleShingles.addTextToShingle(needle);
		needleShingles.setTokenizer(tokenizer);
		needleShingles.compile();
		
		doMatch();
	}
	
	/**
	 * Performs a match operation on the specified needle.
	 * 
	 * @param needle
	 * @see #match(String)
	 */
	public void match(ShingleList needle){
		if(needle.getNGramSize() != nGramSize)
			throw new IllegalArgumentException();
		if(needle.getPreprocessingAlgorithm() != preprocessingAlgorithm)
			throw new IllegalArgumentException();
		
		needleShingles = needle;
		if(! needleShingles.isCompiled())
			needleShingles.compile();
		
		doMatch();
	}
	
	private void doMatch(){
		if(allowMultipleNeedleMatches)
			doMatchSimple();
		else
			doMatchRemoveMultiple();
	}
	
	private void doMatchRemoveMultiple(){
		// build lookup table
		HashMap<String, Integer> needleLookup = new HashMap<String, Integer>();
		for(Shingle s : needleShingles){
			if(! needleLookup.containsKey(s.getShingle()))
				needleLookup.put(s.getShingle(), 1);
			else
				needleLookup.put(s.getShingle(), needleLookup.get(s.getShingle()) + 1);
		}
		
		// build shingle cloud
		shingleCloud = new ArrayList<ShingleCloudMarker>();
		int currentGroupLength = 0;
		for(Shingle s : haystackShingles){
			if(s.isBasicShingle()){
				currentGroupLength++;
				
				Integer n = needleLookup.get(s.getShingle());
				if( n != null && n > 0 ){
					shingleCloud.add(ShingleCloudMarker.Match);
					needleLookup.put(s.getShingle(), n-1);
				} else if (n != null && n < 1)
					shingleCloud.add(ShingleCloudMarker.OutOfMatches);
				else
					shingleCloud.add(ShingleCloudMarker.NoMatch);
			} else if (s.isMagicMatcher()) {
				shingleCloud.add(ShingleCloudMarker.Magic);
				currentGroupLength++;
			} else if(isDetectGroups()) { // group marker
				shingleCloud.add(ShingleCloudMarker.Group);
				if(s.isGroupBegin()){
					currentGroupLength = 0;
				} else if(s.isGroupEnd()) {
					if(null != s.getId())
						groupLengthMap.put(s.getId(), currentGroupLength);
				}
			}
		}

		interpretShingleCloud();
	}
	
	private void doMatchSimple(){
		// build lookup table
		HashSet<String> needleLookup = new HashSet<String>();
		for(Shingle s : needleShingles)
			needleLookup.add(s.getShingle());
		
		// build shingle cloud
		shingleCloud = new ArrayList<ShingleCloudMarker>();
		int currentGroupLength = 0;
		for(Shingle s : haystackShingles){
			if(s.isBasicShingle() ){
				currentGroupLength++;
				if( needleLookup.contains(s.getShingle() ) )
					shingleCloud.add(ShingleCloudMarker.Match);
				else
					shingleCloud.add(ShingleCloudMarker.NoMatch);
			} else if (s.isMagicMatcher()) {
				shingleCloud.add(ShingleCloudMarker.Magic);
				currentGroupLength++;
			} else if(isDetectGroups()){
				shingleCloud.add(ShingleCloudMarker.Group);
				if(s.isGroupBegin()){
					currentGroupLength = 0;
				} else if(s.isGroupEnd()) {
					if(null != s.getId())
						groupLengthMap.put(s.getId(), currentGroupLength);
				}
			}
		}

		interpretShingleCloud();
	}

	private void interpretShingleCloud() {
		
		matches = new ArrayList<ShingleCloudMatch>();
		
		boolean inMatch = false;
        int start = 0;
        int matchLength = 0;
        int numberOfMatchTokensInMatch = 0;
        int seenNoMatchTokens = 0;
        int seenGroupTokens = 0;
        int seenMagicTokens = 0;
        
		for(int i = 0; i< shingleCloud.size(); i++){
			ShingleCloudMarker currentMatch = shingleCloud.get(i);
			
			/* convert outof match into match if it is not alone */
			if( currentMatch == ShingleCloudMarker.OutOfMatches && isAllowNonIsolatedMultipleNeedleMatches()){
				if(inMatch)
					currentMatch = ShingleCloudMarker.Match;
				else {
					int tmp = i;
					while(tmp < shingleCloud.size() - 1 && shingleCloud.get(tmp) == ShingleCloudMarker.OutOfMatches)
						tmp++;
					if(shingleCloud.get(tmp) == ShingleCloudMarker.Match)
						currentMatch = ShingleCloudMarker.Match;
				}
			}
			
			// if we are not matching something and see no one we continue
			if(! inMatch && currentMatch != ShingleCloudMarker.Match)
				continue;
			
			// if we are not matching something and see a one we start a new match
			if(! inMatch && currentMatch == ShingleCloudMarker.Match){
				inMatch = true;
				start = i;
			}
			
			// if we are matching something and see a zero
			if(inMatch && currentMatch == ShingleCloudMarker.NoMatch){
				// do we have to end the match
				if(seenNoMatchTokens == this.maximumNumberOfZerosBetweenMatches){
					// new match?
					if(numberOfMatchTokensInMatch + (seenMagicTokens / magicToOneFactor) >= this.minimumNumberOfOnesInMatch){
						// remove zeros at end of match
						matchLength -= seenNoMatchTokens + seenGroupTokens;

						matches.addAll(ShingleCloudMatch.generateMatches(shingleCloud, start, matchLength, this.haystackShingles, this));
					}
					
					// not matching any more
					inMatch = false;
					matchLength = 0;
					numberOfMatchTokensInMatch = 0;
					seenNoMatchTokens = 0;
					seenGroupTokens = 0;
					seenMagicTokens = 0;
					start = 0;
				} else {
					matchLength++;
					seenNoMatchTokens++;
				}
				continue;
			}
			
			// if we match something and see another 1
			if(inMatch && currentMatch == ShingleCloudMarker.Match){
				numberOfMatchTokensInMatch++;
				seenNoMatchTokens = 0;
				seenGroupTokens = 0;
				matchLength++;
				continue;
			}
			
			// if we are in a match and see a 2
			if( inMatch && currentMatch == ShingleCloudMarker.Group){
				matchLength++;
				seenGroupTokens++;
				continue;
			}
			
			// if we are in a match and found a magic shingle
			if(inMatch && currentMatch == ShingleCloudMarker.Magic){
				if(adjustNeighboringMagic ){
					try{
						int j = 1;
						while(shingleCloud.get(i-(j++)) == ShingleCloudMarker.NoMatch && j <= maximumNumberOfZerosBetweenMatches)
							shingleCloud.set(i-j, ShingleCloudMarker.Magic);
						if(shingleCloud.get(i+1) == ShingleCloudMarker.NoMatch)
							shingleCloud.set(i+1, ShingleCloudMarker.Magic);
					} catch(IndexOutOfBoundsException e){}
				}
				matchLength++;
				seenMagicTokens++;
				continue;
			}
			
		}
		
		// is there a match right at the end
		if(inMatch && numberOfMatchTokensInMatch >= this.minimumNumberOfOnesInMatch){
			// remove zeros at end of match
			matchLength -= seenNoMatchTokens + seenGroupTokens;
			matches.addAll(ShingleCloudMatch.generateMatches(shingleCloud, start, matchLength, this.haystackShingles, this));
		}
		
		// set indirect rating
		for(ShingleCloudMatch m : matches){
			m.setContainmentInNeedle( m.getNumberOfMatchedTokens()/(double) getNeedleShingles().getNumberOfTokens());
			m.setContainmentInHaystack(m.getNumberOfMatchedTokens()/(double) getHaystackShingles().getNumberOfTokens());
				
		}
		
		// sort matches
		if(isSortMatchesByRating())
			Collections.sort(matches);
		
		// remove duplicate matches
		if(isCombineMatchesInGroups() || isDetectGroups()){
			HashMap<Shingle, ShingleCloudMatch> seenMatches = new HashMap<Shingle, ShingleCloudMatch>();
			ArrayList<ShingleCloudMatch> reducedMatches = new ArrayList<ShingleCloudMatch>();
			for(ShingleCloudMatch sm : matches){
				// have we already found a better match?
				if(sm.hasUpperBound() && seenMatches.containsKey(sm.getUpperBound())){
					ShingleCloudMatch betterMatch = seenMatches.get(sm.getUpperBound());
					
					// combine matches
					betterMatch.combine(sm);
					
					continue;
				}
				
				// we have not seen this match yet
				seenMatches.put(sm.getUpperBound(), sm);
				reducedMatches.add(sm);
			}
			matches = reducedMatches;
			
			// sort again
			if(isSortMatchesByRating())
				Collections.sort(matches);
		}
		
		
		// set group rating
		if(isDetectGroups()){
			for(ShingleCloudMatch sm : matches){
				if(sm.hasUpperBound()){
					double length =  (double) (groupLengthMap.get(sm.getUpperBound().getId()) + getNGramSize() - 1);
					if(0 != length){
						sm.setGroupRating(sm.getNumberOfMatchedTokens() / (double) length );
					}
				}
			}
		}
		
	}

	/**
	 * 
	 * @return The n-gram size this object used.
	 */
	public int getNGramSize() {
		return nGramSize;
	}

	/**
	 * 
	 * @param gramSize
	 * 
	 * @throws IllegalStateException The n-gram size cannot be changed after the object was compiled.
	 */
	public void setNGramSize(int gramSize) {
		if(isCompiled())
			throw new IllegalStateException();
		
		nGramSize = gramSize;
	}

	/**
	 * 
	 * @return The minimum number of consecutive MATCH markers needed in the shingle cloud to create a match.
	 */
	public int getMinimumNumberOfOnesInMatch() {
		return minimumNumberOfOnesInMatch;
	}

	/**
	 * Defines the minimum number of consecutive MATCH markers needed in the shingle cloud to create a match.
	 *
	 * <p>
	 * As the shingle cloud algorithm used to only distinguish between matches and no matches
	 * originally a binary string was constructed with 1s denoting a match. 
	 * </p>
	 * 
	 * @param minimumNumberOfOnesInMatch
	 */
	public void setMinimumNumberOfOnesInMatch(int minimumNumberOfOnesInMatch) {
		this.minimumNumberOfOnesInMatch = minimumNumberOfOnesInMatch;
	}

	
	/**
	 * 
	 * @return The minimum number of NoMatch markers needed to divide a match. 
	 */
	public int getMaximumNumberOfZerosBetweenMatches() {
		return maximumNumberOfZerosBetweenMatches;
	}

	/**
	 * Sets the maximum number of consecutive NoMatch markers allowed in one match.
	 * 
	 * <p>
	 * As the shingle cloud algorithm used to only distinguish between matches and no matches
	 * originally a binary string was constructed with 0s denoting a "no match". 
	 * </p>
	 * 
	 * @param maximumNumberOfZerosBetweenMatches
	 */
	public void setMaximumNumberOfZerosBetweenMatches(int maximumNumberOfZerosBetweenMatches) {
		this.maximumNumberOfZerosBetweenMatches = maximumNumberOfZerosBetweenMatches;
	}

	/**
	 * 
	 * @return The preprocessing algorithm that is used by this ShingleCloud algorithm.
	 */
	public Function<String, String> getPreprocessingAlgorithm() {
		return preprocessingAlgorithm;
	}

	/**
	 * Sets the preprocessing algorithm that is to be used with this 
	 * @param preprocessingAlgorithm
	 */
	public void setPreprocessingAlgorithm(Function<String, String> preprocessingAlgorithm) {
		this.preprocessingAlgorithm = preprocessingAlgorithm;
	}

	/**
	 * 
	 * @return Whether or not multiple needle matches are allowed.
	 */
	public boolean isAllowMultipleNeedleMatches() {
		return allowMultipleNeedleMatches;
	}

	/**
	 * Defines whether or not multiple needle matches are allowed.
	 * 
	 * @param allowMultipleNeedleMatches
	 */
	public void setAllowMultipleNeedleMatches(boolean allowMultipleNeedleMatches) {
		this.allowMultipleNeedleMatches = allowMultipleNeedleMatches;
	}

	/**
	 * @return The shingle cloud that resulted from the last match operation
	 */
	public List<ShingleCloudMarker> getShingleCloud(){
		return shingleCloud;
	}
	
	/**
	 * @return The matches from the last match operation
	 * @return
	 */
	public List<ShingleCloudMatch> getMatches(){
		return matches;
	}

	/**
	 * 
	 * @return The tokenizer used when shingling haystack and needle.
	 */
	public Function<String, String[]> getTokenizer() {
		return tokenizer;
	}

	/**
	 * The tokenizer used when shingling haystack and needle.
	 * 
	 * @param tokenizer
	 */
	public void setTokenizer(Function<String, String[]> tokenizer) {
		this.tokenizer = tokenizer;
	}

	/**
	 * 
	 * @return The underlying haystack.
	 */
	public String getHaystack() {
		return haystack;
	}
	
	/**
	 * 
	 * @return The shingles of the underlying haystack.
	 */
	public ShingleList getHaystackShingles(){
		return haystackShingles;
	}

	/**
	 * 
	 * @return The needle's shingles (from the last match operation).
	 */
	public ShingleList getNeedleShingles() {
		return needleShingles;
	}

	/**
	 * 
	 * @return Whether or not to detect groups in matches.
	 */
	public boolean isDetectGroups() {
		return detectGroups;
	}

	/**
	 * Defines whether or not to detect groups in matches.
	 * 
	 * Default: false
	 * 
	 * @param detectGroups
	 */
	public void setDetectGroups(boolean detectGroups) {
		this.detectGroups = detectGroups;
	}

	/**
	 * 
	 * @return Defines whether to split up matches when matching over group boundaries.
	 */
	public boolean isSplitUpMatchesOverGroups() {
		return splitUpMatchesOverGroups;
	}

	/**
	 * Defines whether to split up matches into multiple match objects when matching over group boundaries.
	 * 
	 * Default: true
	 * 
	 * @param splitUpMatchesOverGroups
	 */
	public void setSplitUpMatchesOverGroups(boolean splitUpMatchesOverGroups) {
		this.splitUpMatchesOverGroups = splitUpMatchesOverGroups;
	}

	/**
	 * 
	 * @return true if multiple matches inside the same group are to be combined to one match.
	 */
	public boolean isCombineMatchesInGroups() {
		return combineMatchesInGroups;
	}

	/**
	 * Defines whether multiple matches inside the same group are combined to one match.
	 * 
	 * Default: false
	 * @param combineMatchesInGroups
	 */
	public void setCombineMatchesInGroups(boolean combineMatchesInGroups) {
		this.combineMatchesInGroups = combineMatchesInGroups;
	}

	/**
	 * Defines the number of magic matches that denote another one.
	 * 
	 * Default: 1
	 * @return
	 * @see #setMinimumNumberOfOnesInMatch(int)
	 */
	public int getMagicToOneFactor() {
		return magicToOneFactor;
	}

	/**
	 * Defines the number of magic matches that denote another one.
	 * 
	 * Default: 1
	 * @param magicToOneFactor
	 * @see #setMinimumNumberOfOnesInMatch(int)
	 */
	public void setMagicToOneFactor(int magicToOneFactor) {
		this.magicToOneFactor = magicToOneFactor;
	}

	/**
	 * 
	 * @return The magic words used on the haystack.
	 */
	public String[] getMagicWords() {
		return magicWords;
	}


	/**
	 * Sets magic words that are used on the haystack.
	 * 
	 * @return
	 * @see ShingleList#setMagicWords(String[])
	 */
	public void setMagicWords(String[] magicWords) {
		if(isCompiled())
			throw new IllegalStateException();
		
		this.magicWords = magicWords;
	}

	/**
	 * Defines whether or not to sort matches by rating.
	 * 
	 * Default: true
	 * @return
	 */
	public boolean isSortMatchesByRating() {
		return sortMatchesByRating;
	}

	/**
	 * Defines whether or not to sort matches by rating.
	 * 
	 * Default: true
	 * @param sortMatchesByRating
	 */
	public void setSortMatchesByRating(boolean sortMatchesByRating) {
		this.sortMatchesByRating = sortMatchesByRating;
	}

	public boolean isAdjustNeighboringMagic() {
		return adjustNeighboringMagic;
	}

	/**
	 * If a magic marker is found in a match phase, neighboring noMatch tokens are set to magic.
	 * 
	 * <p>Default: true</p>
	 * @param adjustNeighboringMagic
	 */
	public void setAdjustNeighboringMagic(boolean adjustNeighboringMagic) {
		this.adjustNeighboringMagic = adjustNeighboringMagic;
	}

	public boolean isAllowNonIsolatedMultipleNeedleMatches() {
		return allowNonIsolatedMultipleNeedleMatches;
	}

	public void setAllowNonIsolatedMultipleNeedleMatches(
			boolean allowNonIsolatedMultipleNeedleMatches) {
		this.allowNonIsolatedMultipleNeedleMatches = allowNonIsolatedMultipleNeedleMatches;
	}
	
	/**
	 * The containment score accumulated over all matches.
	 * @return
	 * 
	 * @see ShingleCloudMatch#getContainmentInHaystack()
	 */
	public double getContainmentInHaystack(){
		double containment = 0;
		
		for(ShingleCloudMatch match : getMatches())
			containment += match.getContainmentInHaystack();
		
		return containment;
	}
	
	/**
	 * The containment score accumulated over all matches.
	 * @return
	 * 
	 * @see ShingleCloudMatch#getContainmentInNeedle()
	 */
	public double getContainmentInNeedle(){
		double containment = 0;
		
		for(ShingleCloudMatch match : getMatches())
			containment += match.getContainmentInNeedle();
		
		return containment;
	}
	
	public int getNumberOfMatchingShingles(){
		int matches = 0;
		
		for(ShingleCloudMatch match : getMatches())
			matches += match.getNumberOfMatchedShingles();
		
		return matches;
	}
	
	public int getNumberOfMatchingTokens(){
		int matches = 0;
		
		for(ShingleCloudMatch match : getMatches())
			matches += match.getNumberOfMatchedTokens();
		
		return matches;
	}
	
	/**
	 * Calculates the jaccard measure interpreting the documents as basic sets.
	 * 
	 * NumberOfMatchingShingles/NumberOfUniqueShingles
	 * @return
	 */
	public double getJaccardMeasureForShingles(){
		Map<String, Integer> haystackCount = new HashMap<String, Integer>();
		Map<String, Integer> needleCount = new HashMap<String, Integer>();
		
		for(Shingle shingle : haystackShingles)
			if(shingle.isBasicShingle())
				if(! haystackCount.containsKey(shingle.getShingle()))
					haystackCount.put(shingle.getShingle(), 1);
				else
					haystackCount.put(shingle.getShingle(), haystackCount.get(shingle.getShingle())+1);

		for(Shingle shingle : needleShingles)
			if(shingle.isBasicShingle())
				if(! needleCount.containsKey(shingle.getShingle()))
					needleCount.put(shingle.getShingle(), 1);
				else
					needleCount.put(shingle.getShingle(), needleCount.get(shingle.getShingle())+1);

		int count = 0;
		for(Entry<String, Integer> entry : haystackCount.entrySet()){
			if(needleCount.containsKey(entry.getKey()))
				count += Math.max(needleCount.remove(entry.getKey()), entry.getValue());
			else
				count += entry.getValue();
		}
		
		for(Entry<String, Integer> entry : needleCount.entrySet()){
			count +=entry.getValue();
		}
		
		return getNumberOfMatchingShingles()/(double)count;
	}
	
	/**
	 * Calculates the jaccard measure interpreting the documents as basic sets.
	 * 
	 * NumberOfMatchingShingles/NumberOfUniqueTokens
	 * @return
	 */
	public double getJaccardMeasureForTokens(){
		Map<String, Integer> haystackCount = new HashMap<String, Integer>();
		Map<String, Integer> needleCount = new HashMap<String, Integer>();
		
		for(Shingle shingle : haystackShingles)
			if(shingle.isBasicShingle())
				for(String item : shingle.getItems())
					if(! haystackCount.containsKey(item))
						haystackCount.put(item, 1);
					else
						haystackCount.put(item, haystackCount.get(item)+1);

		for(Shingle shingle : needleShingles)
			if(shingle.isBasicShingle())
				for(String item : shingle.getItems())
					if(! needleCount.containsKey(shingle.getShingle()))
						needleCount.put(item, 1);
					else
						needleCount.put(item, needleCount.get(item)+1);

		int count = 0;
		for(Entry<String, Integer> entry : haystackCount.entrySet()){
			if(needleCount.containsKey(entry.getKey()))
				count += Math.max(needleCount.remove(entry.getKey()), entry.getValue());
			else
				count += entry.getValue();
		}
		
		for(Entry<String, Integer> entry : needleCount.entrySet()){
			count +=entry.getValue();
		}
		
		return getNumberOfMatchingTokens()/(double)count;
	}


}
