package eu.interedition.text.repository;

import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import eu.interedition.text.AnnotationRepository;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.TextRepository;
import eu.interedition.text.rdbms.RelationalText;
import eu.interedition.text.rdbms.RelationalTextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;


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

  @RequestMapping(method = RequestMethod.GET)
  public String uploadForm() {
    return "text_index";
  }

  @RequestMapping(method = RequestMethod.POST, headers="content-type=text/plain")
  public String uploadPlainText(@RequestBody Text text) {
    return redirectTo(text);
  }

  @RequestMapping(method = RequestMethod.POST, headers="content-type=application/xml")
  public String uploadXml(@RequestBody Text text) {
    return redirectTo(text);
  }

  @RequestMapping(method = RequestMethod.POST)
  public String uploadForm(@RequestParam("file") MultipartFile file,//
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
          return redirectTo(textRepository.create(new StreamSource(fileStream = file.getInputStream())));
      }
    } finally {
      Closeables.close(fileStream, false);
    }
    throw new IllegalArgumentException(textType.toString());
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "accept=text/html")
  public ModelAndView view(@PathVariable("id") int id) throws IOException {
    final Text text = textRepository.load(id);

    final ModelAndView mv = new ModelAndView("text");
    mv.addObject("text", text);
    mv.addObject("textContents", textRepository.read(text, new Range(0, (int) Math.min(MAX_TEXT_LENGTH, text.getLength()))));
    mv.addObject("annotationNames", annotationRepository.names(text));
    return mv;
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  @ResponseBody
  public Text download(@PathVariable("id") int id) {
    return textRepository.load(id);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.GET, params = "r")
  public void download(@PathVariable("id") int id, @RequestParam("r") Range range, HttpServletResponse response) throws IOException {
    final Text text = textRepository.load(id);
    final int textLength = (int) text.getLength();

    response.setCharacterEncoding(Text.CHARSET.name());
    response.setContentType(MediaType.TEXT_PLAIN.toString());
    final PrintWriter responseWriter = response.getWriter();

    range = new Range(Math.min(range.getStart(), textLength), Math.min(range.getEnd(), textLength));
    textRepository.read(text, range, new TextRepository.TextReader() {
      @Override
      public void read(Reader content, long contentLength) throws IOException {
        CharStreams.copy(content, responseWriter);
      }
    });
  }

  @ExceptionHandler(value = DataRetrievalFailureException.class)
  public void handleNotFound(HttpServletResponse response) throws IOException {
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  public static String redirectTo(Text text) {
    return "redirect:" + URL_PREFIX + "/" + Long.toString(((RelationalText) text).getId());
  }
}
