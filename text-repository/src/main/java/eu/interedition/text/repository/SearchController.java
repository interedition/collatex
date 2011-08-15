package eu.interedition.text.repository;

import eu.interedition.text.repository.textindex.TextIndex;
import eu.interedition.text.repository.textindex.TextIndexQuery;
import eu.interedition.text.repository.textindex.TextIndexQueryResult;
import org.apache.lucene.queryParser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Transactional
@Controller
@RequestMapping(SearchController.URL_PREFIX)
public class SearchController {
  public static final String URL_PREFIX = "/search";

  @Autowired
  private TextIndex textIndex;

  @RequestMapping(method = RequestMethod.GET)
  public ModelAndView search(@ModelAttribute("query") TextIndexQuery query) throws IOException, ParseException {
    final List<TextIndexQueryResult> searchResults = textIndex.search(query);
    return (searchResults.size() == 1) ?
            new ModelAndView(TextController.redirectTo(searchResults.get(0))) :
            new ModelAndView("search", "results", searchResults);
  }
}
