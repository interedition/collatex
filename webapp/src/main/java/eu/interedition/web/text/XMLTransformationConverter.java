package eu.interedition.web.text;

import eu.interedition.text.rdbms.RelationalNameRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLTransformationConverter implements Converter<String, XMLTransformation> {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Override
  public XMLTransformation convert(String source) {
    final long id = Long.parseLong(source);

    final XMLTransformation xt = jdbcTemplate.queryForObject(new StringBuilder("select ")
            .append(XMLTransformation.select("xt"))
            .append(" from xml_transform xt where xt.id = ?").toString(),
            XMLTransformation.ROW_MAPPER, id);

    xt.setRules(jdbcTemplate.query(new StringBuilder("select ")
            .append(XMLTransformationRule.select("xtr"))
            .append(", ").append(RelationalNameRegistry.selectNameFrom("n"))
            .append(" from xml_transform_rule xtr join text_qname n on xtr.name = n.id where xtr.config = ?").toString(),
            XMLTransformationRule.ROW_MAPPER, id));

    return xt;
  }
}
