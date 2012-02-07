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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import eu.interedition.text.*;
import eu.interedition.text.event.NameCollector;
import eu.interedition.text.json.JSONSerializer;
import eu.interedition.text.query.Criteria;
import eu.interedition.text.rdbms.RelationalText;
import eu.interedition.text.rdbms.RelationalTextRepository;
import eu.interedition.text.xml.XML;
import eu.interedition.text.xml.XMLParser;
import eu.interedition.text.xml.XMLParserModule;
import eu.interedition.text.xml.module.*;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.stax2.XMLInputFactory2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import static eu.interedition.text.query.Criteria.*;


/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Controller
@RequestMapping(TextController.URL_PREFIX)
public class TextController {
  public static final String URL_PREFIX = "/text";
  protected static final int MAX_TEXT_LENGTH = 102400;

  @Autowired
  private TextService textService;

  @Autowired
  private RelationalTextRepository textRepository;

  @Autowired
  private XMLParser xmlParser;

  @Autowired
  private JSONSerializer jsonSerializer;

  @Autowired
  private ObjectMapper objectMapper;

  private XMLInputFactory2 xmlInputFactory = XML.createXMLInputFactory();

  @RequestMapping("/{id}/names")
  @ResponseBody
  public SortedSet<Name> readNames(@PathVariable("id") long id) throws IOException, XMLStreamException {
    return new NameCollector().collect(textRepository, textRepository.load(id)).getNames();
  }

  @RequestMapping(method = RequestMethod.GET)
  public String readIndex() {
    return "text_index";
  }

  @RequestMapping(value = "/{id}", produces = "text/plain")
  public void readText(@PathVariable("id") int id, @RequestParam(value = "r", required = false) Range range, HttpServletResponse response) throws IOException {
    final Text text = textService.load(id).getText();

    response.setCharacterEncoding(Text.CHARSET.name());
    response.setContentType(MediaType.TEXT_PLAIN.toString());
    final PrintWriter responseWriter = response.getWriter();

    range = (range == null ? new Range(0, text.getLength()) : range);
    CharStreams.copy(textRepository.read(text), responseWriter);
  }


  @RequestMapping(value = "/{id}", produces = "application/xml")
  @ResponseBody
  public XMLSerialization readXML(@PathVariable("id") int id) {
    return readXML(id, new XMLSerialization());
  }

  @RequestMapping(value = "/{id}", consumes = "application/json", produces = "application/xml")
  @ResponseBody
  public XMLSerialization readXML(@PathVariable("id") int id, @RequestBody XMLSerialization serialization) {
    serialization.setText(textService.load(id).getText());
    serialization.evaluate();
    return serialization;
  }

  @RequestMapping(value = "/{id}", produces = "application/json")
  @ResponseBody
  public JSONSerialization readJSON(@PathVariable("id") int id, @RequestParam(value = "r", required = false) Range rangeParam) {
    return readJSON(id, new JSONSerialization(), rangeParam);
  }

  @RequestMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
  @ResponseBody
  public JSONSerialization readJSON(@PathVariable("id") int id, @RequestBody JSONSerialization serialization, @RequestParam(value = "r", required = false) Range rangeParam) {
    serialization.setText(textService.load(id).getText());
    if (rangeParam != null) {
      serialization.setRange(rangeParam);
    }
    return serialization;
  }

  @RequestMapping(value = "/{id}", produces = "text/html")
  public ModelAndView readHTML(@PathVariable("id") int id) throws IOException {
    final TextMetadata metadata = textService.load(id);
    final Text text = metadata.getText();

    final ModelAndView mv = new ModelAndView("text");
    mv.addObject("metadata", metadata);
    mv.addObject("text", text);
    mv.addObject("textContents", textRepository.read(text, new Range(0, (int) Math.min(MAX_TEXT_LENGTH, text.getLength()))));
    return mv;
  }

  @RequestMapping(method = RequestMethod.POST, consumes = "text/plain")
  public Object writeText(HttpServletRequest request, Reader requestBody) throws IOException, XMLStreamException, SAXException {
    return respondWith(request, textService.create(new TextMetadata(), requestBody));
  }

  @RequestMapping(method = RequestMethod.POST, consumes = "application/xml")
  public Object writeXML(HttpServletRequest request, InputStream requestBody) throws XMLStreamException, IOException, SAXException {
    XMLStreamReader xmlReader = null;
    try {
      return respondWith(request, textService.create(new TextMetadata(), xmlReader = xmlInputFactory.createXMLStreamReader(requestBody)));
    } finally {
      XML.closeQuietly(xmlReader);
    }
  }

  @RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data")
  public RedirectView writeFormData(@ModelAttribute TextMetadata text, @RequestParam("file") MultipartFile file,//
                                    @RequestParam("fileType") Text.Type textType,//
                                    @RequestParam(value = "fileEncoding", required = false, defaultValue = "UTF-8") String charset)
          throws IOException, XMLStreamException, SAXException {
    Preconditions.checkArgument(!file.isEmpty(), "Empty file");

    InputStream fileStream = null;
    XMLStreamReader xmlReader = null;
    try {
      switch (textType) {
        case TXT:
          return redirectTo(textService.create(text, new InputStreamReader(fileStream = file.getInputStream(), Charset.forName(charset))));
        case XML:
          return redirectTo(textService.create(text, xmlReader = xmlInputFactory.createXMLStreamReader(fileStream = file.getInputStream())));
      }
    } finally {
      XML.closeQuietly(xmlReader);
      Closeables.close(fileStream, false);
    }
    throw new IllegalArgumentException(textType.toString());
  }

  @RequestMapping(value = "/{id}/transform", method = RequestMethod.GET)
  public ModelAndView readTransformationForm(@PathVariable("id") long id) throws XMLStreamException, IOException {
    final TextMetadata metadata = textService.load(id);
    Preconditions.checkArgument(metadata.getText().getType() == Text.Type.XML);

    Map<String, List<String>> names = Maps.newHashMap();
    for (Name name : new NameCollector().collect(textRepository, metadata.getText()).getNames()) {
      final URI namespaceURI = name.getNamespace();
      final String ns = (namespaceURI == null ? "" : namespaceURI.toString());
      List<String> localNames = names.get(ns);
      if (localNames == null) {
        names.put(ns, localNames = Lists.newArrayList());
      }
      localNames.add(name.getLocalName());
    }
    return new ModelAndView("transform").addObject("metadata", metadata).addObject("names", names);
  }

  @RequestMapping(value = "/{id}/transform", method = RequestMethod.POST)
  public Object writeTransformation(@PathVariable("id") long id, HttpServletRequest request, @RequestBody XMLTransformation pc) throws XMLStreamException, IOException, SAXException {
    final TextMetadata source = textService.load(id);
    Preconditions.checkArgument(source.getText().getType() == Text.Type.XML);

    final List<XMLParserModule> modules = pc.getModules();
    modules.add(new LineElementXMLParserModule());
    modules.add(new NotableCharacterXMLParserModule());
    modules.add(new TextXMLParserModule());
    modules.add(new DefaultAnnotationXMLParserModule(textRepository, 1000));
    modules.add(new CLIXAnnotationXMLParserModule(textRepository, 1000));
    if (pc.isTransformTEI()) {
      modules.add(new TEIAwareAnnotationXMLParserModule(textRepository, 1000));
    }

    final Text parsed = xmlParser.parse(source.getText(), pc);
    if (pc.isRemoveEmpty()) {
      textRepository.delete(and(text(parsed), rangeLength(0)));
    }

    final TextMetadata parsedMetadata = new TextMetadata(source);
    parsedMetadata.setCreated(new Date());
    parsedMetadata.setUpdated(parsedMetadata.getCreated());
    return respondWith(request, textService.create(parsedMetadata, (RelationalText) parsed));
  }

  @RequestMapping(value = "/{id}/annotate", method = RequestMethod.POST, consumes = "application/json")
  public Object writeAnnotations(@PathVariable("id") long id, HttpServletRequest request, @RequestBody Reader annotations) throws IOException {
    final TextMetadata metadata = textService.load(id);

    writeAnnotations(metadata.getText(), annotations);
    return respondWith(request, metadata);
  }

  protected void writeAnnotations(Text text, Reader annotations) throws IOException {
    final JsonParser jp = objectMapper.getJsonFactory().createJsonParser(annotations);
    try {
      jsonSerializer.unserialize(jp, text);
    } finally {
      Closeables.close(jp, false);
    }
  }

  @RequestMapping(value = "/{id}/annotate", method = RequestMethod.PUT, consumes = "application/json")
  public Object replaceAnnotations(@PathVariable("id") long id, HttpServletRequest request, @RequestBody Reader annotations) throws IOException {
    final TextMetadata metadata = textService.load(id);
    final RelationalText text = metadata.getText();

    textRepository.delete(Criteria.text(text));
    writeAnnotations(text, annotations);
    return respondWith(request, metadata);
  }


  protected static Object respondWith(HttpServletRequest request, TextMetadata metadata) {
    final List<MediaType> acceptedMediaTypes = MediaType.parseMediaTypes(request.getHeader("Accept"));
    MediaType.sortByQualityValue(acceptedMediaTypes);

    if (acceptedMediaTypes.isEmpty() || !MediaType.APPLICATION_JSON.isCompatibleWith(acceptedMediaTypes.get(0))) {
      final RedirectView redirectView = redirectTo(metadata);
      redirectView.setStatusCode(HttpStatus.CREATED);
      return redirectView;
    } else {
      return new ResponseEntity<Text>(metadata.getText(), HttpStatus.CREATED);
    }
  }

  protected static RedirectView redirectTo(TextMetadata metadata) {
    return new RedirectView(URL_PREFIX + "/" + Long.toString(metadata.getText().getId()), true, false);
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