package de.tud.kom.stringmatching.lcsubsequence;

import java.util.List;

public class LCSResult {

	private int score;
	private List<String> lcs;
	private int[][] fMatrix;
	
	private double containment1;
	private double containment2;
	
	public LCSResult(int score, double containment1, double containment2, List<String> lcs, int[][] fMatrix) {
		this.score = score;
		this.lcs = lcs;
		this.fMatrix = fMatrix;
		this.containment1 = containment1;
		this.containment2 = containment2;
	}

	public int getScore() {
		return score;
	}

	public List<String> getLcs() {
		return lcs;
	}

	public int[][] getFMatrix(){
		return fMatrix;
	}
	
	public double getContainment1() {
		return containment1;
	}

	public void setContainment1(double containment1) {
		this.containment1 = containment1;
	}

	public double getContainment2() {
		return containment2;
	}

	public void setContainment2(double containment2) {
		this.containment2 = containment2;
	}

	public void printResult(){
		for(String t : getLcs())
			System.out.print(t + " ");
		System.out.println();
	}
	
}
