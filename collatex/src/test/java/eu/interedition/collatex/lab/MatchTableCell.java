package eu.interedition.collatex.lab;

/**
 * @author Ronald Haentjens Dekker
 */
public class MatchTableCell {

  private final MatchMatrixCellStatus status;
  private final String text;
  
  public MatchTableCell(MatchMatrixCellStatus status, String text) {
    this.status = status;
    this.text = text;
  }

  public MatchMatrixCellStatus getStatus() {
    return status;
  }

  public String getText() {
    return text;
  }
}
