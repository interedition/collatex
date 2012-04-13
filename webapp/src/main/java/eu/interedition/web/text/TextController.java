/*
 * #%L
 * Text Repository: Datastore for texts based on Interedition's model.
 * %%
 * Copyright (C) 2010 - 2011 The Interedition Development Group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.interedition.web.text;

import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import eu.interedition.text.Name;
import eu.interedition.text.Text;
import eu.interedition.text.TextRange;
import eu.interedition.text.TextTarget;
import eu.interedition.text.util.NameCollector;
import eu.interedition.text.json.JSONSerializer;
import eu.interedition.text.query.QueryCriteria;
import eu.interedition.text.xml.XML;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.stax2.XMLInputFactory2;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.SortedSet;


/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Controller
@RequestMapping("/text")
public class TextController {
  protected static final int MAX_TEXT_LENGTH = 102400;

  @Autowired
  private SessionFactory sessionFactory;

  @Autowired
  private JSONSerializer jsonSerializer;

  @Autowired
  private ObjectMapper objectMapper;

  private XMLInputFactory2 xmlInputFactory = XML.createXMLInputFactory();

  @RequestMapping("/{id}/names")
  @ResponseBody
  public SortedSet<Name> readNames(@PathVariable("id") Text text) throws IOException, XMLStreamException {
    return new NameCollector().collect(sessionFactory.getCurrentSession(), text).getNames();
  }

  @RequestMapping(method = RequestMethod.GET)
  public String readIndex() {
    return "text_index";
  }

  @RequestMapping(value = "/{id}", produces = "text/plain")
  public void readText(@PathVariable("id") Text text, @RequestParam(value = "r", required = false) TextRange range, HttpServletResponse response) throws IOException {
    response.setCharacterEncoding(Text.CHARSET.name());
    response.setContentType(MediaType.TEXT_PLAIN.toString());
    final PrintWriter responseWriter = response.getWriter();

    range = (range == null ? new TextRange(0, text.getLength()) : range);
    CharStreams.copy(text.read(range), responseWriter);
  }


  @RequestMapping(value = "/{id}", produces = "text/html")
  public ModelAndView readHTML(@PathVariable("id") Text text) throws IOException {
    final StringWriter textContents = new StringWriter();
    CharStreams.copy(text.read(new TextRange(0, Math.min(MAX_TEXT_LENGTH, text.getLength()))), textContents);
    return new ModelAndView("text").addObject("text", text).addObject("textContents", textContents);
  }

  @RequestMapping(value = "/{id}/xml", produces = "application/xml")
  @ResponseBody
  public XMLSerialization readXML(@PathVariable("id") Text text) {
    return readXML(text, new XMLSerialization());
  }

  @RequestMapping(value = "/{id}/xml", consumes = "application/json", produces = "application/xml")
  @ResponseBody
  public XMLSerialization readXML(@PathVariable("id") Text text, @RequestBody XMLSerialization serialization) {
    serialization.setText(text);
    serialization.evaluate();
    return serialization;
  }

  @RequestMapping(value = "/{id}/json", produces = "application/json")
  @ResponseBody
  public JSONSerialization readJSON(@PathVariable("id") Text text, @RequestParam(value = "r", required = false) TextTarget range) {
    return readJSON(text, new JSONSerialization(), range);
  }

  @RequestMapping(value = "/{id}/json", consumes = "application/json", produces = "application/json")
  @ResponseBody
  public JSONSerialization readJSON(@PathVariable("id") Text text, @RequestBody JSONSerialization serialization, @RequestParam(value = "r", required = false) TextTarget range) {
    serialization.setText(text);
    if (range != null) {
      serialization.setRange(range);
    }
    return serialization;
  }

  @RequestMapping(method = RequestMethod.POST, consumes = "text/plain", produces = "application/json")
  @ResponseBody
  public ResponseEntity<Text> writeText(Reader requestBody) throws IOException, XMLStreamException, SAXException {
    return new ResponseEntity<Text>(Text.create(sessionFactory.getCurrentSession(), null, requestBody), HttpStatus.CREATED);
  }

  @RequestMapping(method = RequestMethod.POST, consumes = "text/plain", produces = "text/html")
  @ResponseBody
  public RedirectView postText(Reader requestBody) throws IOException, XMLStreamException, SAXException {
    return redirectTo(writeText(requestBody).getBody());
  }

  @RequestMapping(method = RequestMethod.POST, consumes = "application/xml", produces = "application/json")
  public ResponseEntity<Text> writeXML(InputStream requestBody) throws XMLStreamException, IOException, SAXException {
    XMLStreamReader xmlReader = null;
    try {
      return new ResponseEntity<Text>(Text.create(sessionFactory.getCurrentSession(), null, xmlReader = xmlInputFactory.createXMLStreamReader(requestBody)), HttpStatus.CREATED);
    } finally {
      XML.closeQuietly(xmlReader);
    }
  }

  @RequestMapping(method = RequestMethod.POST, consumes = "application/xml", produces = "text/html")
  public RedirectView postXML(InputStream requestBody) throws XMLStreamException, IOException, SAXException {
    return redirectTo(writeXML(requestBody).getBody());
  }

  @RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data", produces = "text/html")
  public RedirectView postTextForm(@RequestParam("file") MultipartFile file,//
                                    @RequestParam("fileType") Text.Type textType,//
                                    @RequestParam(value = "fileEncoding", required = false, defaultValue = "UTF-8") String charset)
          throws IOException, XMLStreamException, SAXException {
    Preconditions.checkArgument(!file.isEmpty(), "Empty file");

    InputStream fileStream = null;
    XMLStreamReader xmlReader = null;
    try {
      switch (textType) {
        case TXT:
          return redirectTo(Text.create(sessionFactory.getCurrentSession(), null, new InputStreamReader(fileStream = file.getInputStream(), Charset.forName(charset))));
        case XML:
          return redirectTo(Text.create(sessionFactory.getCurrentSession(), null, xmlReader = xmlInputFactory.createXMLStreamReader(fileStream = file.getInputStream())));
      }
    } finally {
      XML.closeQuietly(xmlReader);
      Closeables.close(fileStream, false);
    }
    throw new IllegalArgumentException(textType.toString());
  }

  @RequestMapping(value = "/{id}/annotations", method = RequestMethod.POST, consumes = "application/json")
  public Object createAnnotations(@PathVariable("id") Text text, HttpServletRequest request, @RequestBody Reader annotations) throws IOException {
    annotate(text, annotations);
    return respondWith(request, text);
  }

  @RequestMapping(value = "/{id}/annotations", method = RequestMethod.PUT, consumes = "application/json")
  public Object replaceAnnotations(@PathVariable("id") Text text, HttpServletRequest request, @RequestBody Reader annotations) throws IOException {
    QueryCriteria.text(text).delete(sessionFactory.getCurrentSession());
    annotate(text, annotations);
    return respondWith(request, text);
  }

  protected void annotate(Text text, Reader annotations) throws IOException {
    final JsonParser jp = objectMapper.getJsonFactory().createJsonParser(annotations);
    try {
      jsonSerializer.unserialize(jp, text);
    } finally {
      Closeables.close(jp, false);
    }
  }


  protected static Object respondWith(HttpServletRequest request, Text text) {
    final List<MediaType> acceptedMediaTypes = MediaType.parseMediaTypes(request.getHeader("Accept"));
    MediaType.sortByQualityValue(acceptedMediaTypes);

    if (acceptedMediaTypes.isEmpty() || !MediaType.APPLICATION_JSON.isCompatibleWith(acceptedMediaTypes.get(0))) {
      final RedirectView redirectView = redirectTo(text);
      redirectView.setStatusCode(HttpStatus.CREATED);
      return redirectView;
    } else {
      return new ResponseEntity<Text>(text, HttpStatus.CREATED);
    }
  }

  public static RedirectView redirectTo(Text text) {
    return redirectTo(text.getId());
  }

  public static RedirectView redirectTo(long text) {
    return new RedirectView("/text/" + Long.toString(text), true, false);
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
  }

  @ExceptionHandler(value = DataRetrievalFailureException.class)
  public void handleNotFound(HttpServletResponse response) throws IOException {
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }
}