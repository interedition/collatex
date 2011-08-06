package eu.interedition.text.query;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AndOperator extends Operator {
  AndOperator() {
    super();
  }

  AndOperator(Iterable<Criterion> operands) {
    super(operands);
  }
}
