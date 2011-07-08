package eu.interedition.text.repository;

import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;
import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.rdbms.RelationalText;
import eu.interedition.text.rdbms.RelationalTextRepository;
import eu.interedition.text.xml.XMLParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;


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
  private XMLParser xmlParser;

  @Autowired
  private RelationalTextRepository textRepository;

  @Autowired
  private AnnotationRepository annotationRepository;

  @RequestMapping(method = RequestMethod.PUT)
  public String upload(@RequestBody Text text) {
    return redirectTo(text);
  }

  @RequestMapping(method = RequestMethod.GET)
  public String uploadForm() {
    return "text_index";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String upload(@RequestParam("file") MultipartFile file,//
                       @RequestParam("fileType") Text.Type textType,//
                       @RequestParam(value = "fileEncoding", required = false, defaultValue = "UTF-8") String charset)
          throws IOException, TransformerException {
    Preconditions.checkArgument(!file.isEmpty(), "Empty file");

    InputStream fileStream = null;
    try {
      switch (textType) {
        case PLAIN:
          return redirectTo(textRepository.create(new InputStreamReader(fileStream = file.getInputStream(), Charset.forName(charset))));
        case XML:
          return redirectTo(xmlParser.load(new StreamSource(fileStream = file.getInputStream())));
      }
    } finally {
      Closeables.close(fileStream, false);
    }
    throw new IllegalArgumentException(textType.toString());
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
