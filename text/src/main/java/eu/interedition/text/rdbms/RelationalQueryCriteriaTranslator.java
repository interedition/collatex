package eu.interedition.text.rdbms;

import com.google.common.base.Objects;
import eu.interedition.text.QNameRepository;
import eu.interedition.text.Range;
import eu.interedition.text.query.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class RelationalQueryCriteriaTranslator {

  private QNameRepository nameRepository;

  public void setNameRepository(QNameRepository nameRepository) {
    this.nameRepository = nameRepository;
  }

  public StringBuilder where(StringBuilder sql, Criterion criterion, Collection<Object> ps) {
    return sql(sql.append(" where "), criterion, ps);
  }

  protected StringBuilder sql(StringBuilder sql, Criterion criterion, Collection<Object> ps) {
    if (criterion instanceof Operator) {
      sql(sql, (Operator) criterion, ps);
    } else if (criterion instanceof AnnotationNameCriterion) {
      sql(sql, (AnnotationNameCriterion) criterion, ps);
    } else if (criterion instanceof TextCriterion) {
      sql(sql, (TextCriterion) criterion, ps);
    } else if (criterion instanceof AnnotationLinkNameCriterion) {
      sql(sql, (AnnotationLinkNameCriterion) criterion, ps);
    } else if (criterion instanceof RangeOverlapCriterion) {
      sql(sql, (RangeOverlapCriterion) criterion, ps);
    } else if (criterion instanceof RangeLengthCriterion) {
      sql(sql, (RangeLengthCriterion) criterion, ps);
    } else if (criterion instanceof AnnotationIdentityCriterion) {
      sql((AnnotationIdentityCriterion) criterion, ps, sql);
    } else if (criterion instanceof NotOperator) {
      sql(sql, (NotOperator) criterion, ps);
    } else if (criterion instanceof AnyCriterion) {
      sql(sql, (AnyCriterion) criterion, ps);
    } else if (criterion instanceof NoneCriterion) {
      sql(sql, (NoneCriterion) criterion, ps);
    } else {
      throw new IllegalArgumentException(Objects.toStringHelper(criterion).toString());
    }

    return sql;
  }

  protected void sql(StringBuilder sql, Operator op, Collection<Object> ps) {
    final List<Criterion> operands = op.getOperands();
    if (operands.isEmpty()) {
      return;
    }
    sql.append("(");
    for (Iterator<Criterion> pIt = operands.iterator(); pIt.hasNext(); ) {
      sql(sql, pIt.next(), ps);
      if (pIt.hasNext()) {
        sql.append(" ").append(sqlOperator(op)).append(" ");
      }
    }
    sql.append(")");
  }

  protected String sqlOperator(Operator op) {
    if (op instanceof AndOperator) {
      return "and";
    } else if (op instanceof OrOperator) {
      return "or";
    } else {
      throw new IllegalArgumentException();
    }
  }

  protected void sql(StringBuilder sql, NotOperator criterion, Collection<Object> ps) {
    sql.append("(NOT ");
    where(sql, criterion.getOperand(), ps);
    sql.append(")");
  }

  protected void sql(StringBuilder sql, AnnotationLinkNameCriterion criterion, Collection<Object> ps) {
    sql.append("(al.name = ?)");
    ps.add(((RelationalQName)nameRepository.get(criterion.getName())).getId());
  }

  protected void sql(AnnotationIdentityCriterion criterion, Collection<Object> ps, StringBuilder sql) {
    sql.append("(a.id = ?)");
    ps.add(((RelationalAnnotation) criterion.getAnnotation()).getId());
  }

  protected void sql(StringBuilder sql, AnnotationNameCriterion criterion, Collection<Object> ps) {
    sql.append("(a.name = ?)");
    ps.add(((RelationalQName)nameRepository.get(criterion.getName())).getId());
  }

  protected void sql(StringBuilder sql, TextCriterion criterion, Collection<Object> ps) {
    sql.append("(a.text = ?)");
    ps.add(((RelationalText) criterion.getText()).getId());
  }

  protected void sql(StringBuilder sql, RangeOverlapCriterion criterion, Collection<Object> ps) {
    final Range range = criterion.getRange();
    sql.append("(a.range_start < ? and a.range_end > ?)");
    ps.add(range.getEnd());
    ps.add(range.getStart());
  }

  protected void sql(StringBuilder sql, RangeLengthCriterion criterion, Collection<Object> ps) {
    sql.append("(a.range_end - a.range_start = ?)");
    ps.add(criterion.getLength());
  }

  protected void sql(StringBuilder sql, AnyCriterion criterion, Collection<Object> ps) {
    sql.append("(1 = 1)");
  }

  protected void sql(StringBuilder sql, NoneCriterion criterion, Collection<Object> ps) {
    sql.append("(1 <> 1)");
  }


}
