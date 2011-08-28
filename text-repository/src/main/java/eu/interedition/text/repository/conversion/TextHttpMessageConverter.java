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
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import eu.interedition.text.Text;
import eu.interedition.text.TextRepository;
import eu.interedition.text.repository.TextService;
import eu.interedition.text.repository.model.TextImpl;
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

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.Charset;

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
  private TransformerFactory transformerFactory;

  public TextHttpMessageConverter() {
    super(MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return TextImpl.class.isAssignableFrom(clazz);
  }

  @Override
  protected TextImpl readInternal(Class<? extends TextImpl> clazz, final HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    final MediaType contentType = inputMessage.getHeaders().getContentType();
    final Charset contentTypeCharset = contentType.getCharSet();
    final Charset charset = (contentTypeCharset == null ? Text.CHARSET : contentTypeCharset);

    try {
      return new TransactionTemplate(transactionManager).execute(new TransactionCallback<TextImpl>() {
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
            } else {
              return textService.create(new TextImpl(), new StreamSource(inputMessage.getBody()));
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
      final TransactionTemplate tt = new TransactionTemplate(transactionManager);
      tt.setReadOnly(true);
      tt.execute(new TransactionCallbackWithoutResult() {
        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
          try {
            textRepository.read(text, new TextRepository.TextReader() {
              @Override
              public void read(Reader content, long contentLength) throws IOException {
                switch (text.getType()) {
                  case TXT:
                    OutputStreamWriter bodyWriter = null;
                    try {
                      CharStreams.copy(content, bodyWriter = new OutputStreamWriter(outputMessage.getBody(), charset));
                    } finally {
                      Closeables.close(bodyWriter, false);
                    }
                    break;
                  case XML:
                    OutputStream bodyStream = null;
                    try {
                      transformerFactory.newTransformer().transform(new StreamSource(content),//
                              new StreamResult(bodyStream = outputMessage.getBody()));
                    } catch (TransformerException e) {
                      throw Throwables.propagate(e);
                    } finally {
                      Closeables.close(bodyStream, false);
                    }
                    break;
                }
              }
            });
          } catch (IOException e) {
            throw Throwables.propagate(e);
          }
        }
      });
    } catch (Throwable t) {
      final Throwable cause = Throwables.getRootCause(t);
      if (cause instanceof TransformerException) {
        throw new HttpMessageNotWritableException(((TransformerException) cause).getMessageAndLocation(), cause);
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
    transformerFactory = TransformerFactory.newInstance();
  }
}
