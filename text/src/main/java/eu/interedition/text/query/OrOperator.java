package eu.interedition.text.query;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class OrOperator extends Operator {
  OrOperator() {
    super();
  }

  OrOperator(Iterable<Criterion> operands) {
    super(operands);
  }
}
