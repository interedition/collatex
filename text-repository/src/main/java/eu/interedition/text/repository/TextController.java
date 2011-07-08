package eu.interedition.text.repository;

import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.rdbms.RelationalText;
import eu.interedition.text.rdbms.RelationalTextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Controller
@Transactional
@RequestMapping(TextController.URL_PREFIX)
public class TextController {
  protected static final String URL_PREFIX = "/text";
  protected static final int MAX_TEXT_LENGTH = 102400;

  @Autowired
  private RelationalTextRepository textRepository;

  @Autowired
  private AnnotationRepository annotationRepository;

  @RequestMapping(method = RequestMethod.PUT)
  public String upload(@RequestBody Text text) {
    return redirectTo(text);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  @ResponseBody
  public Text download(@PathVariable("id") int id) {
    return textRepository.load(id);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "accept=text/html")
  public ModelAndView view(@PathVariable("id") int id) throws IOException {
    final Text text = textRepository.load(id);
    final int length = textRepository.length(text);

    final ModelAndView mv = new ModelAndView("text");
    mv.addObject("text", text);
    mv.addObject("textLength", length);
    mv.addObject("textContents", textRepository.read(text, new Range(0, Math.min(MAX_TEXT_LENGTH, length))));
    mv.addObject("annotationNames", annotationRepository.names(text));
    return mv;
  }

  @ExceptionHandler(value = DataRetrievalFailureException.class)
  public void handleNotFound(HttpServletResponse response) throws IOException {
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  public static String redirectTo(Text text) {
    return "redirect:" + URL_PREFIX + "/" + Integer.toString(((RelationalText) text).getId());
  }
}
