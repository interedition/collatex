package eu.interedition.web.text;

import eu.interedition.text.Name;
import eu.interedition.text.util.SQL;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLTransformationRule {
  private Name name;
  private boolean lineElement;
  private boolean containerElement;
  private boolean included;
  private boolean excluded;
  private boolean notable;

  public XMLTransformationRule() {
  }

  public XMLTransformationRule(Name name, boolean lineElement, boolean containerElement, boolean included, boolean excluded, boolean notable) {
    this.name = name;
    this.lineElement = lineElement;
    this.containerElement = containerElement;
    this.included = included;
    this.excluded = excluded;
    this.notable = notable;
  }

  public Name getName() {
    return name;
  }

  public void setName(Name name) {
    this.name = name;
  }

  public boolean isContainerElement() {
    return containerElement;
  }

  public void setContainerElement(boolean containerElement) {
    this.containerElement = containerElement;
  }

  public boolean isLineElement() {
    return lineElement;
  }

  public void setLineElement(boolean lineElement) {
    this.lineElement = lineElement;
  }

  public boolean isIncluded() {
    return included;
  }

  public void setIncluded(boolean included) {
    this.included = included;
  }

  public boolean isExcluded() {
    return excluded;
  }

  public void setExcluded(boolean excluded) {
    this.excluded = excluded;
  }

  public boolean isNotable() {
    return notable;
  }

  public void setNotable(boolean notable) {
    this.notable = notable;
  }

  public boolean isEmpty() {
    return !(containerElement || lineElement || included || excluded || notable);
  }

  public static String select(String tableName) {
    return SQL.select(tableName, "is_line", "is_container", "is_excluded", "is_included", "is_notable");
  }

  public static final RowMapper<XMLTransformationRule> ROW_MAPPER = new RowMapper<XMLTransformationRule>() {

    @Override
    public XMLTransformationRule mapRow(ResultSet rs, int rowNum) throws SQLException {
      final XMLTransformationRule xtr = new XMLTransformationRule();
      xtr.setName(null); // FIXME!!
      xtr.setLineElement(rs.getBoolean("xtr_is_line"));
      xtr.setContainerElement(rs.getBoolean("xtr_is_container"));
      xtr.setExcluded(rs.getBoolean("xtr_is_excluded"));
      xtr.setIncluded(rs.getBoolean("xtr_is_included"));
      xtr.setNotable(rs.getBoolean("xtr_is_notable"));
      return xtr;
    }
  };

}
