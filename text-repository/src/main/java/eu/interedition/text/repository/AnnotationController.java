package eu.interedition.text.repository;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import eu.interedition.text.Annotation;
import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.QName;
import eu.interedition.text.Range;
import eu.interedition.text.query.Operator;
import eu.interedition.text.repository.model.AnnotationImpl;
import eu.interedition.text.repository.model.QNameImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.SortedSet;

import static eu.interedition.text.query.Criteria.*;

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
  private IndexingTextRepository textRepository;

  @RequestMapping("/{id}/names")
  @ResponseBody
  public SortedSet<QName> names(@PathVariable("id") long id) {
    final SortedSet<QName> names = annotationRepository.names(textRepository.load(id));
    return Sets.<QName>newTreeSet(Iterables.transform(names, QNameImpl.TO_BEAN));
  }

  @RequestMapping(value = "/{id}")
  @ResponseBody
  public SortedSet<Annotation> get(@PathVariable("id") long id, @RequestParam(value = "r", required = false) Range range) {
    final Operator criterion = and(text(textRepository.load(id)));
    if (range != null) {
      criterion.add(rangeOverlap(range));
    }
    final Iterable<Annotation> annotations = annotationRepository.find(criterion);
    return Sets.<Annotation>newTreeSet(Iterables.transform(annotations, AnnotationImpl.TO_BEAN));
  }
}
