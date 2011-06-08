package eu.interedition.collatex.persistence;

import com.google.common.base.Strings;
import eu.interedition.collatex2.experimental.MyNewCollateXEngine;
import eu.interedition.collatex2.implementation.CollateXEngine;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.junit.Assert;
import org.junit.Test;
import org.lmnl.AnnotationRepository;
import org.lmnl.Text;
import org.lmnl.TextRepository;
import org.lmnl.rdbms.RelationalAnnotationFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Transactional
public class SimpleCollationTest extends AbstractTest {

  @Autowired
  private RelationalAnnotationFactory annotationFactory;

  @Autowired
  private AnnotationRepository annotationRepository;

  @Autowired
  private TextRepository textRepository;

  @Autowired
  private SessionFactory sessionFactory;

  private MyNewCollateXEngine engine = new MyNewCollateXEngine();

  @Test
  public void collate() throws IOException {
    final Session session = sessionFactory.getCurrentSession();

    final Collation collation = new Collation();
    session.save(collation);

    for (int i = 1; i <= 5; i++) {
      final Text text = annotationFactory.newText();

      final String textContent = Strings.repeat("bla", i);
      textRepository.write(text, new StringReader(textContent), textContent.length());

      final Witness witness = new Witness();
      witness.setCollation(collation);
      witness.setText(text);
      session.save(witness);
    }
  }
}
