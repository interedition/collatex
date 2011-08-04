package eu.interedition.text.repository;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.QName;
import eu.interedition.text.rdbms.RelationalQName;
import eu.interedition.text.rdbms.RelationalTextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Controller
@Transactional
@RequestMapping(AnnotationController.URL_PREFIX)
public class AnnotationController {
  protected static final String URL_PREFIX = "/annotation";

  @Autowired
  private AnnotationRepository annotationRepository;

  @Autowired
  private RelationalTextRepository textRepository;

  @RequestMapping("/names/{id}")
  @ResponseBody
  public SortedSet<QName> names(@PathVariable("id") int id) {
    return Sets.newTreeSet(Iterables.transform(annotationRepository.names(textRepository.load(id)), new Function<QName, QName>() {

      @Override
      public QName apply(QName input) {
        final QNameBean returnValue = new QNameBean();
        returnValue.setNamespaceURI(input.getNamespaceURI());
        returnValue.setLocalName(input.getLocalName());
        if (input instanceof RelationalQName) {
          returnValue.setId(Long.toString(((RelationalQName) input).getId()));
        }
        return returnValue;
      }
    }));
  }
}
