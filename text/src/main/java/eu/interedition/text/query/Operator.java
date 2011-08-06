package eu.interedition.text.query;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class Operator implements Criterion {
  protected final List<Criterion> operands;

  protected Operator() {
    this(Collections.<Criterion>emptyList());
  }

  protected Operator(Iterable<Criterion> operands) {
    this.operands = Lists.newArrayList(operands);
  }

  public Operator add(Criterion operand) {
    this.operands.add(operand);
    return this;
  }

  public List<Criterion> getOperands() {
    return operands;
  }
}
