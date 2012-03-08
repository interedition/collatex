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
package eu.interedition.web.metadata;

import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import eu.interedition.text.Text;
import eu.interedition.text.TextRepository;
import eu.interedition.text.rdbms.RelationalText;
import eu.interedition.text.util.SQL;
import eu.interedition.text.xml.XML;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 * @see <a href="http://tools.ietf.org/html/rfc5013">RFC5013: The Dublin Core Metadata Element Set</a>
 */
public class DublinCoreMetadata {
  private long text;
  private DateTime created;
  private DateTime updated;
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

  public DublinCoreMetadata() {
  }

  public DublinCoreMetadata(DateTime dateTime) {
    this.created = dateTime;
    this.updated = dateTime;
    this.date = dateTime.toString();
  }
  public DublinCoreMetadata(DublinCoreMetadata other) {
    update(other);
  }

  public long getText() {
    return text;
  }

  public void setText(long text) {
    this.text = text;
  }

  public DateTime getCreated() {
    return created;
  }

  public void setCreated(DateTime created) {
    this.created = created;
  }

  public void setCreated(Date created) {
    setCreated(created == null ? null : new DateTime(created));
  }

  public DateTime getUpdated() {
    return updated;
  }

  public void setUpdated(DateTime updated) {
    this.updated = updated;
  }

  public void setUpdated(Date updated) {
    setUpdated(updated == null ? null : new DateTime(updated));
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
    if (created != null) {
      document.add(new Field("created", Long.toString(created.getMillis()), Field.Store.YES, Field.Index.NOT_ANALYZED));
    }
    if (updated != null) {
      document.add(new Field("updated", Long.toString(updated.getMillis()), Field.Store.YES, Field.Index.NOT_ANALYZED));
    }
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

  public DublinCoreMetadata update(TextRepository repository, Text source) throws IOException, XMLStreamException {
    if (source.getType() != Text.Type.XML) {
      return this;
    }

    Reader textReader = null;
    XMLStreamReader xmlReader = null;
    try {
      textReader = repository.read(source).getInput();
      xmlReader = XML.createXMLInputFactory().createXMLStreamReader(textReader);

      boolean inTitleStmt = false;
      StringBuilder textContent = null;
      String localName;
      while (xmlReader.hasNext()) {
        switch (xmlReader.next()) {
          case XMLStreamReader.START_ELEMENT:
            localName = xmlReader.getLocalName();
            if ("titleStmt".equals(localName)) {
              inTitleStmt = true;
            } else if (inTitleStmt && "title".equals(localName)) {
              textContent = new StringBuilder();
            } else if (inTitleStmt && "author".equals(localName)) {
              textContent = new StringBuilder();
            }
            break;
          case XMLStreamReader.END_ELEMENT:
            localName = xmlReader.getLocalName();
            if ("titleStmt".equals(localName)) {
              return this;
            } else if (inTitleStmt && "title".equals(localName)) {
              title = textContent.toString().replaceAll("\\s+", " ");
              textContent = null;
            } else if (inTitleStmt && "author".equals(localName)) {
              creator = textContent.toString().replaceAll("\\s+", " ");
              textContent = null;
            }
            break;
          case XMLStreamReader.CHARACTERS:
          case XMLStreamReader.CDATA:
              if (textContent != null) {
                textContent.append(xmlReader.getText());
              }
        }
      }
      return this;
    } finally {
      XML.closeQuietly(xmlReader);
      Closeables.close(textReader, false);
    }
  }

  public void update(DublinCoreMetadata updated) {
    this.created = updated.created;
    this.updated = updated.updated;
    this.title = updated.title;
    this.creator = updated.creator;
    this.subject = updated.subject;
    this.description = updated.description;
    this.publisher = updated.publisher;
    this.contributor = updated.contributor;
    this.date = updated.date;
    this.type = updated.type;
    this.format = updated.format;
    this.identifier = updated.identifier;
    this.source = updated.source;
    this.language = updated.language;
  }

  public MapSqlParameterSource toSqlParameterSource() {
    return new MapSqlParameterSource()
            .addValue("text", text)
            .addValue("text_created", created == null ? null : created.toDate())
            .addValue("text_updated", updated == null ? null : updated.toDate())
            .addValue("text_title", title)
            .addValue("text_creator", creator)
            .addValue("text_subject", subject)
            .addValue("text_description", description)
            .addValue("text_publisher", publisher)
            .addValue("text_contributor", contributor)
            .addValue("text_date", date)
            .addValue("text_type", type)
            .addValue("text_format", format)
            .addValue("text_identifier", identifier)
            .addValue("text_source", source)
            .addValue("text_language", language);
  }

  public static String selectMetadataFrom(String tableName) {
    return SQL.select(tableName, "text", "text_created", "text_updated", "text_title", "text_creator", "text_subject", "text_description", "text_publisher", "text_contributor", "text_date", "text_type", "text_format", "text_identifier", "text_source", "text_language");
  }

  public static DublinCoreMetadata mapMetadataFrom(ResultSet rs, String prefix) throws SQLException {
    final DublinCoreMetadata metadata = new DublinCoreMetadata();
    metadata.setText(rs.getLong(prefix + "_text"));
    metadata.setCreated(rs.getTimestamp(prefix + "_text_created"));
    metadata.setUpdated(rs.getTimestamp(prefix + "_text_updated"));
    metadata.setTitle(rs.getString(prefix + "_text_title"));
    metadata.setCreator(rs.getString(prefix + "_text_creator"));
    metadata.setSubject(rs.getString(prefix + "_text_subject"));
    metadata.setDescription(rs.getString(prefix + "_text_description"));
    metadata.setPublisher(rs.getString(prefix + "_text_publisher"));
    metadata.setContributor(rs.getString(prefix + "_text_contributor"));
    metadata.setDate(rs.getString(prefix + "_text_date"));
    metadata.setType(rs.getString(prefix + "_text_type"));
    metadata.setFormat(rs.getString(prefix + "_text_format"));
    metadata.setIdentifier(rs.getString(prefix + "_text_identifier"));
    metadata.setSource(rs.getString(prefix + "_text_source"));
    metadata.setLanguage(rs.getString(prefix + "_text_language"));
    return metadata;
  }
}
