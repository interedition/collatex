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

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.text.Text;
import eu.interedition.text.TextConsumer;
import eu.interedition.text.rdbms.RelationalText;
import eu.interedition.text.rdbms.RelationalTextRepository;
import eu.interedition.text.util.SQL;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static eu.interedition.text.rdbms.RelationalTextRepository.mapTextFrom;
import static eu.interedition.text.rdbms.RelationalTextRepository.selectTextFrom;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Service
@Transactional
public class TextService implements InitializingBean {

  @Autowired
  private RelationalTextRepository textRepository;

  @Autowired
  private TextIndex textIndex;

  @Autowired
  private DataSource dataSource;

  private SimpleJdbcInsert metadataInsert;
  private JdbcTemplate jt;
  private SAXParserFactory saxParserFactory;

  public TextMetadata load(long id) {
    return DataAccessUtils.requiredUniqueResult(load(Collections.singleton(id)));
  }

  public List<TextMetadata> load(Iterable<Long> ids) {
    final List<Long> idList = Lists.newArrayList(ids);
    if (idList.isEmpty()) {
      return Collections.emptyList();
    }

    final StringBuilder sql = sql();
    sql.append(" where t.id in (");
    for (int ic = 0; ic < idList.size(); ic++) {
      sql.append(ic == 0 ? "" : ", ").append("?");
    }
    sql.append(")");

    return jt.query(sql.toString(), new TextMetadataRowMapper(), idList.toArray(new Object[idList.size()]));
  }

  public long count() {
    return jt.queryForLong("select count(*) from text_metadata");
  }

  public List<TextMetadata> list(long page, long pageSize) {
    return jt.query(sql().append(" order by tm.text_updated desc limit ? offset ?").toString(), new TextMetadataRowMapper(), pageSize, page * pageSize);
  }

  public void scroll(final TextScroller scroller) {
    jt.query(sql().toString(), new RowMapper<Void>() {
      private RowMapper<TextMetadata> textMapper = new TextMetadataRowMapper();

      @Override
      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        scroller.text(textMapper.mapRow(rs, rowNum));
        return null;
      }
    });
  }

  protected StringBuilder sql() {
    final StringBuilder sql = new StringBuilder("select");
    sql.append(" ").append(selectTextFrom("t"));
    sql.append(", ").append(selectMetadataFrom("tm"));
    sql.append(", ").append(selectCollectionFrom("tc"));
    sql.append(" from text_content t");
    sql.append(" join text_metadata tm on t.id = tm.text");
    return sql;
  }

  public TextMetadata create(TextMetadata text, Reader content) throws IOException, SAXException {
    return create(text, (RelationalText) textRepository.create(content));
  }

  public TextMetadata create(TextMetadata text, XMLStreamReader xml) throws IOException, SAXException, XMLStreamException {
    return create(text, (RelationalText) textRepository.create(xml));
  }

  protected TextMetadata create(TextMetadata metadata, RelationalText text) throws IOException, SAXException {
    metadata.setText(text);
    metadata.setCreated(new Date());
    metadata.setUpdated(metadata.getCreated());

    if (text.getType() == Text.Type.XML && metadata.isEmpty()) {
      extractMetadata(metadata);
    }
    metadataInsert.execute(new MapSqlParameterSource()
            .addValue("text", text.getId())
            .addValue("text_created", metadata.getCreated())
            .addValue("text_updated", metadata.getUpdated())
            .addValue("text_title", metadata.getTitle())
            .addValue("text_creator", metadata.getCreator())
            .addValue("text_subject", metadata.getSubject())
            .addValue("text_description", metadata.getDescription())
            .addValue("text_publisher", metadata.getPublisher())
            .addValue("text_contributor", metadata.getContributor())
            .addValue("text_date", metadata.getDate())
            .addValue("text_type", metadata.getType())
            .addValue("text_format", metadata.getFormat())
            .addValue("text_identifier", metadata.getIdentifier())
            .addValue("text_source", metadata.getSource())
            .addValue("text_language", metadata.getLanguage()));

    textIndex.update(metadata);
    return metadata;
  }

  protected void extractMetadata(final TextMetadata text) throws IOException, SAXException {
    try {
    textRepository.read(text.getText(), new TextConsumer() {
      @Override
      public void read(Reader content, long contentLength) throws IOException {
        try {
          saxParserFactory.newSAXParser().parse(new InputSource(content), new DefaultHandler() {
            private boolean inTitleStmt = false;
            private StringBuilder textContent;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
              if ("titleStmt".equals(localName) && text.isEmpty()) {
                inTitleStmt = true;
              } else if (inTitleStmt && "title".equals(localName)) {
                textContent = new StringBuilder();
              } else if (inTitleStmt && "author".equals(localName)) {
                textContent = new StringBuilder();
              }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
              if ("titleStmt".equals(localName)) {
                inTitleStmt = false;
              } else if (inTitleStmt && "title".equals(localName)) {
                text.setTitle(textContent());
              } else if (inTitleStmt && "author".equals(localName)) {
                text.setCreator(textContent());
              }
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
              if (textContent != null) {
                textContent.append(ch, start, length);
              }
            }

            protected String textContent() {
              final String str = textContent.toString().replaceAll("\\s+", " ");
              textContent = null;
              return str;
            }
          });
        } catch (SAXException e) {
          Throwables.propagate(e);
        } catch (ParserConfigurationException e) {
          Throwables.propagate(e);
        }
      }
    });
    } catch (Throwable t) {
      Throwables.propagateIfInstanceOf(t, SAXException.class);
      Throwables.propagate(t);
    }
  }

  public void delete(TextMetadata text) {
    try {
      textRepository.delete(text.getText());
      textIndex.delete(text);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  public void write(TextMetadata text, Reader contents, long contentLength) throws IOException {
    textRepository.write(text.getText(), contents, contentLength);
    textIndex.update(text);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.metadataInsert = new SimpleJdbcInsert(dataSource).withTableName("text_metadata");
    this.jt = new JdbcTemplate(dataSource);
    this.saxParserFactory = SAXParserFactory.newInstance();
    this.saxParserFactory.setValidating(false);
    this.saxParserFactory.setNamespaceAware(true);
    this.saxParserFactory.setXIncludeAware(false);
  }

  public static String selectMetadataFrom(String tableName) {
    return SQL.select(tableName, "text_created", "text_updated", "text_title", "text_creator", "text_subject", "text_description", "text_publisher", "text_contributor", "text_date", "text_type", "text_format", "text_identifier", "text_source", "text_language");
  }

  public static String selectCollectionFrom(String tableName) {
    return SQL.select(tableName, "id", "name");
  }

  public static TextMetadata mapMetadata(ResultSet rs, String prefix, RelationalText text) throws SQLException {
    final TextMetadata metadata = new TextMetadata();
    metadata.setText(text);
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

  private static class TextMetadataRowMapper implements RowMapper<TextMetadata> {
    @Override
    public TextMetadata mapRow(ResultSet rs, int rowNum) throws SQLException {
      return mapMetadata(rs, "tm", mapTextFrom(rs, "t"));
    }
  }

  public interface TextScroller {
    void text(TextMetadata text);
  }
}
