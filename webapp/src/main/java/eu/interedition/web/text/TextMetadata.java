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

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import eu.interedition.text.rdbms.RelationalText;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Date;

/**
 *
 * @see <a href="http://tools.ietf.org/html/rfc5013">RFC5013: The Dublin Core Metadata Element Set</a>
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 *
 */
public class TextMetadata {
  private RelationalText text;
  private Date created;
  private Date updated;
  private String title;
  private String creator;
  private String subject;
  private String description;
  private String publisher;
  private String contributor;
  private String date;
  private String type;
  private String format;
  private String identifier;
  private String source;
  private String language;

  public TextMetadata() {
  }

  public TextMetadata(TextMetadata other) {
    this.text = other.text;
    this.created = other.created;
    this.updated = other.updated;
    this.title = other.title;
    this.creator = other.creator;
    this.subject = other.subject;
    this.description = other.description;
    this.publisher = other.publisher;
    this.contributor = other.contributor;
    this.date = other.date;
    this.type = other.type;
    this.format = other.format;
    this.identifier = other.identifier;
    this.source = other.source;
    this.language = other.language;
  }

  public RelationalText getText() {
    return text;
  }

  public void setText(RelationalText text) {
    this.text = text;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public Date getUpdated() {
    return updated;
  }

  public void setUpdated(Date updated) {
    this.updated = updated;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public String getContributor() {
    return contributor;
  }

  public void setContributor(String contributor) {
    this.contributor = contributor;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isEmpty() {
    return Iterables.all(Lists.newArrayList(title, creator, subject, description, publisher, contributor, date, type, format, identifier, source, language), Predicates.isNull());
  }

  public void addTo(Document document) {
    document.add(new Field("created", Long.toString(created.getTime()), Field.Store.YES, Field.Index.NOT_ANALYZED));
    document.add(new Field("updated", Long.toString(updated.getTime()), Field.Store.YES, Field.Index.NOT_ANALYZED));
    if (title != null) {
      document.add(new Field("title", title, Field.Store.NO, Field.Index.ANALYZED));
    }
    if (description != null) {
      document.add(new Field("description", description, Field.Store.NO, Field.Index.ANALYZED));
    }
    if (creator != null) {
      document.add(new Field("creator", creator, Field.Store.NO, Field.Index.ANALYZED));
    }
  }
}
