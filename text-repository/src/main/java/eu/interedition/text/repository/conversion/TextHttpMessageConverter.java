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
package eu.interedition.text.repository.conversion;

import com.google.common.base.Throwables;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import eu.interedition.text.*;
import eu.interedition.text.json.JSONSerializer;
import eu.interedition.text.json.JSONSerializerConfiguration;
import eu.interedition.text.mem.SimpleQName;
import eu.interedition.text.query.Criteria;
import eu.interedition.text.query.Criterion;
import eu.interedition.text.repository.TextService;
import eu.interedition.text.repository.model.TextImpl;
import eu.interedition.text.xml.XMLSerializer;
import eu.interedition.text.xml.XMLSerializerConfiguration;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextHttpMessageConverter extends AbstractHttpMessageConverter<TextImpl> implements InitializingBean {

  @Autowired
  private TextService textService;

  @Autowired
  private TextRepository textRepository;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private JSONSerializer jsonSerializer;

  @Autowired
  private XMLSerializer xmlSerializer;

  @Autowired
  private ObjectMapper objectMapper;

  private SAXTransformerFactory transformerFactory;
  private JsonFactory jsonFactory;

  private TransactionTemplate writingTransactionTemplate;
  private TransactionTemplate readingTransactionTemplate;

  public TextHttpMessageConverter() {
    super(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return TextImpl.class.isAssignableFrom(clazz);
  }

  @Override
  protected boolean canWrite(MediaType mediaType) {
    return false;
  }

  @Override
  protected TextImpl readInternal(Class<? extends TextImpl> clazz, final HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    final MediaType contentType = inputMessage.getHeaders().getContentType();
    final Charset contentTypeCharset = contentType.getCharSet();
    final Charset charset = (contentTypeCharset == null ? Text.CHARSET : contentTypeCharset);

    try {
      return writingTransactionTemplate.execute(new TransactionCallback<TextImpl>() {
        @Override
        public TextImpl doInTransaction(TransactionStatus status) {
          try {
            if (MediaType.TEXT_PLAIN.isCompatibleWith(contentType)) {
              Reader textContent = null;
              try {
                return textService.create(new TextImpl(), textContent = new InputStreamReader(inputMessage.getBody(), charset));
              } finally {
                Closeables.close(textContent, false);
              }
            } else if (MediaType.APPLICATION_XML.isCompatibleWith(contentType)) {
              return textService.create(new TextImpl(), new StreamSource(inputMessage.getBody()));
            } else {
              throw new HttpMessageNotReadableException("Cannot read text encoded as " + contentType.toString());
            }
          } catch (IOException e) {
            throw Throwables.propagate(e);
          } catch (TransformerException e) {
            throw Throwables.propagate(e);
          }
        }
      });
    } catch (Throwable t) {
      final Throwable cause = Throwables.getRootCause(t);
      if (cause instanceof TransformerException) {
        throw new HttpMessageNotReadableException(((TransformerException) cause).getMessageAndLocation(), cause);
      }
      Throwables.propagateIfInstanceOf(cause, IOException.class);
      Throwables.propagateIfInstanceOf(cause, HttpMessageNotReadableException.class);
      throw Throwables.propagate(t);
    }
  }

  @Override
  protected void writeInternal(final TextImpl text, final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    final MediaType contentType = outputMessage.getHeaders().getContentType();
    final Charset contentTypeCharset = contentType.getCharSet();
    final Charset charset = (contentTypeCharset == null ? Text.CHARSET : contentTypeCharset);

    try {
      readingTransactionTemplate.execute(new TransactionCallbackWithoutResult() {
        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
          OutputStream bodyStream = null;
          try {
            if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
              final JsonGenerator jg = jsonFactory.createJsonGenerator(bodyStream = outputMessage.getBody());
              try {
                jsonSerializer.serialize(jg, text, JSON_SERIALIZER_CONFIGURATION);
              } finally {
                Closeables.close(jg, false);
              }
            } else if (MediaType.TEXT_PLAIN.isCompatibleWith(contentType)) {
              final Writer bodyWriter = new OutputStreamWriter(bodyStream = outputMessage.getBody(), charset);
              textRepository.read(text, new TextRepository.TextReader() {
                @Override
                public void read(Reader content, long contentLength) throws IOException {
                  CharStreams.copy(content, bodyWriter);
                  bodyWriter.flush();
                }
              });
            } else if (MediaType.APPLICATION_XML.isCompatibleWith(contentType)) {
              final StreamResult xml = new StreamResult(bodyStream = outputMessage.getBody());
              if (text.getType() == Text.Type.XML) {
                textRepository.read(text, xml);
              } else {
                final TransformerHandler th = transformerFactory.newTransformerHandler();
                th.setResult(xml);
                xmlSerializer.serialize(th, text, XML_SERIALIZER_CONFIGURATION);
              }
            } else {
              throw new HttpMessageNotWritableException(contentType.toString());
            }
          } catch (XMLStreamException e) {
            throw Throwables.propagate(e);
          } catch (TransformerException e) {
            throw Throwables.propagate(e);
          } catch (IOException e) {
            throw Throwables.propagate(e);
          } finally {
            Closeables.closeQuietly(bodyStream);
          }
        }
      });
    } catch (Throwable t) {
      final Throwable cause = Throwables.getRootCause(t);
      if (cause instanceof TransformerException) {
        throw new HttpMessageNotWritableException(((TransformerException) cause).getMessageAndLocation(), cause);
      }
      if (cause instanceof XMLStreamException) {
        throw new HttpMessageNotWritableException(cause.getMessage(), cause);
      }
      Throwables.propagateIfInstanceOf(cause, IOException.class);
      Throwables.propagateIfInstanceOf(cause, HttpMessageNotWritableException.class);
      throw Throwables.propagate(t);
    }
  }

  @Override
  protected MediaType getDefaultContentType(TextImpl text) {
    switch (text.getType()) {
      case TXT:
        return MediaType.TEXT_PLAIN;
      case XML:
        return MediaType.APPLICATION_XML;
    }

    throw new IllegalStateException(text.getType().toString());
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    transformerFactory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
    jsonFactory = objectMapper.getJsonFactory();

    writingTransactionTemplate = new TransactionTemplate(transactionManager);

    readingTransactionTemplate = new TransactionTemplate(transactionManager);
    readingTransactionTemplate.setReadOnly(true);
  }

  private static final JSONSerializerConfiguration JSON_SERIALIZER_CONFIGURATION = new JSONSerializerConfiguration() {
    @Override
    public Range getRange() {
      return null;
    }

    @Override
    public Map<String, URI> getNamespaceMappings() {
      return Maps.newHashMap(NAMESPACE_MAP);
    }

    @Override
    public Set<QName> getDataSet() {
      return null;
    }

    @Override
    public Criterion getQuery() {
      return Criteria.any();
    }
  };

  private static final XMLSerializerConfiguration XML_SERIALIZER_CONFIGURATION = new XMLSerializerConfiguration() {
    @Override
    public QName getRootName() {
      return new SimpleQName(TextConstants.INTEREDITION_NS_URI, "xml");
    }

    @Override
    public Map<String, URI> getNamespaceMappings() {
      return Maps.newHashMap(NAMESPACE_MAP);
    }

    @Override
    public List<QName> getHierarchy() {
      return null;
    }

    @Override
    public Criterion getQuery() {
      return Criteria.any();
    }
  };

  private static final BiMap<String, URI> NAMESPACE_MAP = HashBiMap.create();

  static {
    NAMESPACE_MAP.put("xml", TextConstants.XML_NS_URI);
    NAMESPACE_MAP.put("tei", TextConstants.TEI_NS);
    NAMESPACE_MAP.put("ie", TextConstants.INTEREDITION_NS_URI);
    NAMESPACE_MAP.put("clix", TextConstants.CLIX_NS);
  }
}
