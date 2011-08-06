package eu.interedition.text.query;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class NotOperator implements Criterion {
  private final Criterion operand;

  NotOperator(Criterion operand) {
    this.operand = operand;
  }

  public Criterion getOperand() {
    return operand;
  }
}
