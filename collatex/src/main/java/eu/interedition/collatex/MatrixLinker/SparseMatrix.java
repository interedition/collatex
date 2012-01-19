package eu.interedition.collatex.MatrixLinker;

import java.util.ArrayList;

import com.google.common.collect.ArrayTable;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraphVertex;

public class SparseMatrix  {
	
	
	private ArrayTable<VariantGraphVertex, Token, Boolean> sparseMatrix;

	public SparseMatrix(Iterable<VariantGraphVertex> vertices, Iterable<Token> witness) {
    sparseMatrix = ArrayTable.create(vertices,witness);
	}

	

	public void set(int row, int column, boolean value) {
		sparseMatrix.set(row, column, value);
  }

	public boolean at(int row, int column) {
		Boolean result = sparseMatrix.at(row,column);
	  if(result==null)
	  	return false;
	  return result;
  }
	
  @Override
  public String toString() {
  	String result = "";
  	for(Token t : sparseMatrix.columnKeyList()) {
		  String token = t.toString();
		  int pos = token.lastIndexOf(":");
		  if(pos>-1) {
		    String woord = token.substring(pos+2, token.length()-1);
	      result += " " + woord;
		  }
  	}
  	result += "\n";
  	int colNum = sparseMatrix.columnKeyList().size();
  	ArrayList<String> labels = rowLabels();
  	int row = 0;
  	for(String label : labels) {
      result += label;
      for(int col=0; col<colNum;col++)
      	result += " " + at(row++,col);
      result += "\n";
  	}
		return result;
  }

  public String toHtml() {
  	String result = "<table>\n<tr><td></td>\n";
  	for(Token t : sparseMatrix.columnKeyList()) {
		  String token = t.toString();
		  int pos = token.lastIndexOf(":");
		  if(pos>-1) {
		    String woord = token.substring(pos+2, token.length()-1);
	      result += "<td>" + woord + "</td>";
		  }
  	}
  	result += "</tr>\n";
  	int colNum = sparseMatrix.columnKeyList().size();
  	ArrayList<String> labels = rowLabels();
  	int row = 0;
  	for(String label : labels) {
      result += "<tr><td>" + label + "</td>";
      for(int col=0; col<colNum;col++)
      	if(at(row,col))
      		result += "<td BGCOLOR=\"lightgreen\">M</td>";
      	else
      		result += "<td></td>";
      result += "</tr>\n";
      row++;
	  }
  	result += "</table>";
		return result;
  }
  
  public ArrayList<String> rowLabels() {
  	ArrayList<String> labels = new ArrayList<String>();
  	for(VariantGraphVertex vgv : sparseMatrix.rowKeyList()) {
		  String token = vgv.toString();
		  int pos = token.lastIndexOf(":");
		  if(pos>-1) {
		  	labels.add(token.substring(pos+2, token.length()-2));
		  }
  	}
    return labels;
  }

}
