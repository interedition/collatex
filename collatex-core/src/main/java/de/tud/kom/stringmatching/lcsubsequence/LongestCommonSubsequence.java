package de.tud.kom.stringmatching.lcsubsequence;

import java.util.ArrayList;
import java.util.List;

import de.tud.kom.stringutils.preprocessing.Preprocess;
import de.tud.kom.stringutils.tokenization.CharacterTokenizer;
import de.tud.kom.stringutils.tokenization.Tokenizer;

public class LongestCommonSubsequence {
	
	protected Tokenizer tokenizer = new CharacterTokenizer();
	protected Preprocess preprocessor = null;
	protected boolean backtrace = false;
	
	
	public LCSResult lcs(String sequence1, String sequence2){
		/* preprocess */
		if(null != preprocessor){
			sequence1 = preprocessor.preprocessInput(sequence1);
			sequence2 = preprocessor.preprocessInput(sequence2);
		}
		
		/* get tokens */
		String[] tokens1 = tokenizer.tokenize(sequence1);
		String[] tokens2 = tokenizer.tokenize(sequence2);
		int n1 = tokens1.length;
		int n2 = tokens2.length;
		
		/* initialize matrix */
		int[][] fMatrix = new int[n1+1][n2+1];
		
		/* fill matrix */
		for(int i = 1; i < fMatrix.length; i++) {
			for(int j = 1; j < fMatrix[i].length; j++){ 
				if(tokens1[i-1].equals(tokens2[j-1])) 
					fMatrix[i][j] = fMatrix[i-1][j-1] + 1;
				else
					fMatrix[i][j] = Math.max(fMatrix[i][j-1], fMatrix[i-1][j]);
			}
		}

		/* initialize score */
		int score = fMatrix[n1][n2];
		
		/* backtrace */
		List<String> backtraceResult = null;
		if(backtrace){
			backtraceResult = new ArrayList<String>();
			backtrace(backtraceResult, fMatrix, tokens1, tokens2, n1, n2);
		}
		
		/* create result object */
		LCSResult result = new LCSResult(score, score/(double)tokens1.length, score / (double) tokens2.length, backtraceResult, fMatrix);
		
		return result;
	}
	

	private void backtrace(List<String> result, int[][]fMatrix, String[] tokens1, String[] tokens2, int i, int j) {
		if(i == 0 || j == 0)
			return;
		
		if(tokens1[i-1].equals(tokens2[j-1])){
			backtrace(result, fMatrix, tokens1, tokens2, i-1,j-1);
			result.add(tokens1[i-1]);
		} else if(fMatrix[i][j-1] > fMatrix[i-1][j])
			backtrace(result, fMatrix, tokens1, tokens2, i,j-1);
		else
			backtrace(result, fMatrix, tokens1, tokens2, i-1,j);
	}




	public Tokenizer getTokenizer() {
		return tokenizer;
	}
	public void setTokenizer(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}
	public Preprocess getPreprocessor() {
		return preprocessor;
	}
	public void setPreprocessor(Preprocess preprocessor) {
		this.preprocessor = preprocessor;
	}


	public boolean isBacktrace() {
		return backtrace;
	}


	public void setBacktrace(boolean backtrace) {
		this.backtrace = backtrace;
	}
	
	
}
