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
package eu.interedition.text.repository.atom;

import eu.interedition.text.Text;
import eu.interedition.text.repository.TextController;
import eu.interedition.text.repository.TextService;
import eu.interedition.text.repository.model.TextImpl;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Content;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Person;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.context.ResponseContextException;
import org.apache.abdera.protocol.server.impl.AbstractEntityCollectionAdapter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Node;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
@Transactional
public class TextCollectionAdapter extends AbstractEntityCollectionAdapter<TextImpl> implements InitializingBean {
  public static final String HREF = "text";
  public static final String TITLE = "Texts";

  @Autowired
  private TextService textService;

  @Override
  public TextImpl postEntry(String title, IRI id, String summary, Date updated, List<Person> authors, Content content, RequestContext request) throws ResponseContextException {
    Text.Type textType;
    switch (content.getContentType()) {
      case TEXT:
        textType = Text.Type.TXT;
        break;
      case XML:
        textType = Text.Type.XML;
        break;
      default:
        throw new ResponseContextException(ProviderHelper.notallowed(request));
    }

    final TextImpl text = new TextImpl(textType);
    text.setTitle(title);
    text.setSummary(summary);
    text.setUpdated(updated);
    text.setAuthor(authors.isEmpty() ? null : authors.get(0).getName());

    try {
      if (textType == Text.Type.TXT) {
        return textService.create(text, new StringReader(content.getValue()));
      } else {
        return textService.create(text, new DOMSource((Node) content.getValueElement()));
      }
    } catch (IOException e) {
      throw new ResponseContextException(ProviderHelper.servererror(request, e));
    } catch (TransformerException e) {
      throw new ResponseContextException(ProviderHelper.servererror(request, e));
    }
  }

  @Override
  public void deleteEntry(String resourceName, RequestContext request) throws ResponseContextException {
    textService.delete(getEntry(resourceName, request));
  }

  @Override
  public Object getContent(TextImpl entry, RequestContext request) throws ResponseContextException {
    return null;
  }

  @Override
  public Iterable<TextImpl> getEntries(RequestContext request) throws ResponseContextException {
    return textService.list(0, 1000);
  }

  @Override
  public TextImpl getEntry(String resourceName, RequestContext request) throws ResponseContextException {
    try {
      return textService.load(Long.parseLong(resourceName));
    } catch (NumberFormatException e) {
      throw new ResponseContextException(ProviderHelper.badrequest(request, e.getMessage()));
    }
  }

  @Override
  public String getId(TextImpl entry) throws ResponseContextException {
    return Long.toString(entry.getId());
  }

  @Override
  public String getName(TextImpl entry) throws ResponseContextException {
    return Long.toString(entry.getId());
  }

  @Override
  public String getTitle(TextImpl entry) throws ResponseContextException {
    return new StringBuilder("[").append(entry.getType()).append("] ").append(entry.getDescription()).toString();
  }

  @Override
  public Date getUpdated(TextImpl entry) throws ResponseContextException {
    return entry.getUpdated();
  }

  @Override
  public void putEntry(TextImpl entry, String title, Date updated, List<Person> authors, String summary, Content content, RequestContext request) throws ResponseContextException {
    throw new ResponseContextException(ProviderHelper.notallowed(request));
  }

  @Override
  public String getAuthor(RequestContext request) throws ResponseContextException {
    return null;
  }

  @Override
  public String getId(RequestContext request) {
    return HREF;
  }

  @Override
  public String getTitle(RequestContext request) {
    return TITLE;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    setHref(HREF);
  }

  @Override
  protected String addEntryDetails(RequestContext request, Entry e, IRI feedIri, TextImpl text) throws ResponseContextException {
    final String link = super.addEntryDetails(request, e, feedIri, text);
    e.addLink(request.getContextPath()  + TextController.URL_PREFIX + "/" + Long.toString(text.getId()));
    if (text.getSummary() != null) {
      e.setSummary(text.getSummary());
    }
    return link;
  }
}
