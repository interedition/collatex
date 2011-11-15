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

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.text.Text;
import eu.interedition.text.rdbms.RelationalText;
import eu.interedition.text.rdbms.RelationalTextRepository;
import eu.interedition.text.repository.model.TextCollection;
import eu.interedition.text.repository.model.TextImpl;
import eu.interedition.text.repository.textindex.TextIndex;
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
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
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

  public TextImpl load(long id) {
    return DataAccessUtils.requiredUniqueResult(load(Collections.singleton(id)));
  }

  public List<TextImpl> load(Iterable<Long> ids) {
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

    return jt.query(sql.toString(), new TextImplRowMapper(), idList.toArray(new Object[idList.size()]));
  }

  public long count() {
    return jt.queryForLong("select count(*) from repository_text_metadata");
  }

  public List<TextImpl> list(long page, long pageSize) {
    return jt.query(sql().append(" order by tm.updated desc limit ? offset ?").toString(), new TextImplRowMapper(), pageSize, page * pageSize);
  }

  public void scroll(final TextScroller scroller) {
    jt.query(sql().toString(), new RowMapper<Void>() {
      private RowMapper<TextImpl> textMapper = new TextImplRowMapper();

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
    sql.append(" join repository_text_metadata tm on t.id = tm.text");
    sql.append(" left join repository_text_collection tc on tm.collection = tc.id");
    return sql;
  }

  public TextImpl create(TextImpl text, Reader content) throws IOException {
    return create(text, (RelationalText) textRepository.create(content));
  }

  public TextImpl create(TextImpl text, Source xml) throws IOException, XMLStreamException {
    return create(text, (RelationalText) textRepository.create(xml));
  }

  protected TextImpl create(TextImpl text, RelationalText model) throws IOException {
    text = new TextImpl(model.getType(), model.getLength(), model.getDigest(), model.getId());

    if (model.getType() == Text.Type.XML && text.isWithoutMetadata()) {
      extractMetadata(text);
    }
    metadataInsert.execute(new MapSqlParameterSource()
            .addValue("text", model.getId())
            .addValue("created", text.getCreated())
            .addValue("updated", text.getUpdated())
            .addValue("collection", text.getCollection() == null ? null : text.getCollection().getId())
            .addValue("title", text.getTitle())
            .addValue("summary", text.getSummary())
            .addValue("author", text.getAuthor()));

    textIndex.update(text);
    return text;
  }

  protected void extractMetadata(final TextImpl text) throws IOException {
    try {
      textRepository.read(text, new SAXResult(new DefaultHandler() {
        private boolean inTitleStmt = false;
        private StringBuilder textContent;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
          if ("titleStmt".equals(localName) && text.isWithoutMetadata()) {
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
            text.setAuthor(textContent());
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
      }));
    } catch (TransformerException e) {
      throw Throwables.propagate(e);
    }
  }

  public void delete(TextImpl text) {
    try {
      textRepository.delete(text);
      textIndex.delete(text);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  public void write(TextImpl text, Reader contents, long contentLength) throws IOException {
    textRepository.write(text, contents, contentLength);
    textIndex.update(text);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.metadataInsert = new SimpleJdbcInsert(dataSource).withTableName("repository_text_metadata");
    this.jt = new JdbcTemplate(dataSource);
  }

  public static String selectMetadataFrom(String tableName) {
    return SQL.select(tableName, "created", "updated", "title", "summary", "author");
  }

  public static String selectCollectionFrom(String tableName) {
    return SQL.select(tableName, "id", "name");
  }

  public static TextImpl mapMetadata(ResultSet rs, String prefix, RelationalText model, TextCollection collection) throws SQLException {
    final TextImpl text = new TextImpl(model);
    text.setCollection(collection);
    text.setCreated(rs.getTimestamp(prefix + "_created"));
    text.setUpdated(rs.getTimestamp(prefix + "_updated"));
    text.setTitle(rs.getString(prefix + "_title"));
    text.setSummary(rs.getString(prefix + "_summary"));
    text.setAuthor(rs.getString(prefix + "_author"));
    return text;
  }

  public static TextCollection mapCollection(ResultSet rs, String prefix) throws SQLException {
    final TextCollection collection = new TextCollection();
    collection.setId(rs.getLong(prefix + "_id"));
    collection.setName(rs.getString(prefix + "_name"));
    return collection;
  }

  private static class TextImplRowMapper implements RowMapper<TextImpl> {
    private Map<Long, TextCollection> collections = Maps.newHashMap();

    @Override
    public TextImpl mapRow(ResultSet rs, int rowNum) throws SQLException {
      TextCollection collection = mapCollection(rs, "tc");
      final long collectionId = collection.getId();
      if (collectionId == 0) {
        collection = null;
      } else if (collections.containsKey(collectionId)) {
        collection = collections.get(collectionId);
      } else {
        collections.put(collectionId, collection);
      }

      return mapMetadata(rs, "tm", mapTextFrom(rs, "t"), collection);
    }
  }

  public interface TextScroller {
    void text(TextImpl text);
  }
}
