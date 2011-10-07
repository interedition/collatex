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
package eu.interedition.text.repository;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import eu.interedition.text.*;
import eu.interedition.text.rdbms.RelationalText;
import eu.interedition.text.rdbms.RelationalTextRepository;
import eu.interedition.text.repository.model.*;
import eu.interedition.text.xml.XMLParser;
import eu.interedition.text.xml.XMLParserModule;
import eu.interedition.text.xml.module.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
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
@Transactional
@RequestMapping(TextController.URL_PREFIX)
public class TextController {
  public static final String URL_PREFIX = "/text";
  protected static final int MAX_TEXT_LENGTH = 102400;

  @Autowired
  private TextService textService;

  @Autowired
  private RelationalTextRepository textRepository;

  @Autowired
  private AnnotationRepository annotationRepository;

  @Autowired
  private XMLParser xmlParser;

  @RequestMapping(value = "/{id}", headers = "accept=text/html")
  public ModelAndView getTextAsHtml(@PathVariable("id") int id) throws IOException {
    final TextImpl text = textService.load(id);

    final ModelAndView mv = new ModelAndView("text");
    mv.addObject("text", text);
    mv.addObject("textContents", textRepository.read(text, new Range(0, (int) Math.min(MAX_TEXT_LENGTH, text.getLength()))));
    return mv;
  }

  @RequestMapping(value = "/{id}", headers = "accept=text/plain")
  public void getTextAsPlainText(@PathVariable("id") int id, @RequestParam(value= "r", required = false) Range range, HttpServletResponse response) throws IOException {
    final TextImpl text = textService.load(id);

    response.setCharacterEncoding(Text.CHARSET.name());
    response.setContentType(MediaType.TEXT_PLAIN.toString());
    final PrintWriter responseWriter = response.getWriter();

    range = (range == null ? new Range(0, text.getLength()) : range);
    textRepository.read(text, range, new TextRepository.TextReader() {
      @Override
      public void read(Reader content, long contentLength) throws IOException {
        CharStreams.copy(content, responseWriter);
      }
    });
  }


  @RequestMapping(value = "/{id}", headers = "accept=application/xml")
  @ResponseBody
  public XMLSerialization getTextAsXml(@PathVariable("id") int id) {
    return getTextAsXml(id, new XMLSerialization());
  }

  @RequestMapping(value = "/{id}", headers = {"content-type=application/json", "accept=application/xml"})
  @ResponseBody
  public XMLSerialization getTextAsXml(@PathVariable("id") int id, @RequestBody XMLSerialization serialization) {
    serialization.setText(textService.load(id));
    serialization.evaluate();
    return serialization;
  }

  @RequestMapping(value = "/{id}", headers = "accept=application/json")
  @ResponseBody
  public JSONSerialization getTextAsJson(@PathVariable("id") int id, @RequestParam(value = "r", required = false) Range rangeParam) {
    return getTextAsJson(id, new JSONSerialization(), rangeParam);
  }

  @RequestMapping(value = "/{id}", headers = {"content-type=application/json","accept=application/json"})
  @ResponseBody
  public JSONSerialization getTextAsJson(@PathVariable("id") int id, @RequestBody JSONSerialization serialization, @RequestParam(value = "r", required = false) Range rangeParam) {
    serialization.setText(textService.load(id));
    if (rangeParam != null) {
      serialization.setRange(rangeParam);
    }
    return serialization;
  }

  @RequestMapping("/{id}/names")
  @ResponseBody
  public SortedSet<QName> getNamesOfText(@PathVariable("id") long id) {
    return Sets.<QName>newTreeSet(Iterables.transform(annotationRepository.names(textRepository.load(id)), QNameImpl.TO_BEAN));
  }

  @RequestMapping(value = "/{id}/transform", method = RequestMethod.GET)
  public ModelAndView getTransformationForm(@PathVariable("id") long id) {
    final TextImpl text = textService.load(id);
    Preconditions.checkArgument(text.getType() == Text.Type.XML);

    Map<String, List<String>> names = Maps.newHashMap();
    for (QName name : annotationRepository.names(text)) {
      final URI namespaceURI = name.getNamespaceURI();
      final String ns = (namespaceURI == null ? "" : namespaceURI.toString());
      List<String> localNames = names.get(ns);
      if (localNames == null) {
        names.put(ns, localNames = Lists.newArrayList());
      }
      localNames.add(name.getLocalName());
    }
    return new ModelAndView("transform").addObject("text", text).addObject("names", names);
  }

  @RequestMapping(value = "/{id}/transform", method = RequestMethod.POST)
  @ResponseBody
  public Text transform(@PathVariable("id") long id, @RequestBody XMLTransformation pc) throws XMLStreamException, IOException {
    final TextImpl source = textService.load(id);
    Preconditions.checkArgument(source.getType() == Text.Type.XML);

    final List<XMLParserModule> modules = pc.getModules();
    modules.add(new LineElementXMLParserModule());
    modules.add(new NotableCharacterXMLParserModule());
    modules.add(new TextXMLParserModule(textRepository));
    modules.add(new DefaultAnnotationXMLParserModule(annotationRepository, 1000));
    modules.add(new CLIXAnnotationXMLParserModule(annotationRepository, 1000));
    if (pc.isTransformTEI()) {
      modules.add(new TEIAwareAnnotationXMLParserModule(annotationRepository, 1000));
    }

    final Text parsed = xmlParser.parse(source, pc);
    if (pc.isRemoveEmpty()) {
      annotationRepository.delete(and(text(parsed), rangeLength(0)));
    }

    final TextImpl parsedMetadata = new TextImpl(source);
    parsedMetadata.setCreated(new Date());
    parsedMetadata.setUpdated(parsedMetadata.getCreated());
    return textService.create(parsedMetadata, (RelationalText) parsed);
  }

  @RequestMapping(method = RequestMethod.GET)
  public String getIndex() {
    return "text_index";
  }

  @RequestMapping(method = RequestMethod.POST, headers = "content-type=text/plain")
  public RedirectView postTextPlainText(@RequestBody Text text) {
    return created(text);
  }

  @RequestMapping(method = RequestMethod.POST, headers = "content-type=application/xml")
  public RedirectView postTextAsXml(@RequestBody Text text) {
    return created(text);
  }

  @RequestMapping(method = RequestMethod.POST)
  public RedirectView postTextViaForm(@ModelAttribute TextImpl text, @RequestParam("file") MultipartFile file,//
                                 @RequestParam("fileType") Text.Type textType,//
                                 @RequestParam(value = "fileEncoding", required = false, defaultValue = "UTF-8") String charset)
          throws IOException, TransformerException {
    Preconditions.checkArgument(!file.isEmpty(), "Empty file");

    InputStream fileStream = null;
    try {
      switch (textType) {
        case TXT:
          return redirectTo(textService.create(text, new InputStreamReader(fileStream = file.getInputStream(), Charset.forName(charset))));
        case XML:
          return redirectTo(textService.create(text, new StreamSource(fileStream = file.getInputStream())));
      }
    } finally {
      Closeables.close(fileStream, false);
    }
    throw new IllegalArgumentException(textType.toString());
  }

  @ExceptionHandler(value = DataRetrievalFailureException.class)
  public void handleNotFound(HttpServletResponse response) throws IOException {
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  public static RedirectView redirectTo(Text text) {
    return new RedirectView(URL_PREFIX + "/" + Long.toString(((RelationalText) text).getId()), true, false);
  }

  public static RedirectView created(Text text) {
    final RedirectView redirectView = redirectTo(text);
    redirectView.setStatusCode(HttpStatus.CREATED);
    return redirectView;
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
  }
}
