package eu.interedition.web.text;

import eu.interedition.text.Text;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextConverter implements Converter<String, Text> {

  @Autowired
  private SessionFactory sessionFactory;

  @Override
  public Text convert(String source) {
    return (Text) sessionFactory.getCurrentSession().load(Text.class, Long.parseLong(source));
  }
}
