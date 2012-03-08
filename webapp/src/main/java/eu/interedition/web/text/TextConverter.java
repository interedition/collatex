package eu.interedition.web.text;

import eu.interedition.text.Text;
import eu.interedition.text.rdbms.RelationalNameRegistry;
import eu.interedition.text.rdbms.RelationalText;
import eu.interedition.text.rdbms.RelationalTextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextConverter implements Converter<String, RelationalText> {

  @Autowired
  private RelationalTextRepository repository;

  @Override
  public RelationalText convert(String source) {
    return repository.read(Long.parseLong(source));
  }
}
