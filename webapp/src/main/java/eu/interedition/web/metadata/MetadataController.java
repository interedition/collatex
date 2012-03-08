package eu.interedition.web.metadata;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import eu.interedition.text.rdbms.RelationalText;
import eu.interedition.text.util.SQL;
import eu.interedition.web.index.IndexController;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Controller
@RequestMapping("/metadata")
public class MetadataController implements InitializingBean {

  @Autowired
  private DataSource dataSource;

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  private IndexController indexController;

  private SimpleJdbcInsert metadataInsert;

  @RequestMapping(value = "/{id}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
  @ResponseBody
  public DublinCoreMetadata create(@PathVariable("id") RelationalText text, @RequestBody DublinCoreMetadata metadata) {
    try {
      return read(text);
    } catch (EmptyResultDataAccessException e) {
      metadataInsert.execute(metadata.toSqlParameterSource());
      return metadata;
    }
  }

  @RequestMapping(value = "/{id}", produces = "application/json")
  @ResponseBody
  public DublinCoreMetadata read(@PathVariable("id") RelationalText text) {
    return jdbcTemplate.getJdbcOperations().queryForObject(sql().append(" where md.text = ?").toString(), ROW_MAPPER, text.getId());
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
  @ResponseBody
  public DublinCoreMetadata update(@PathVariable("id") RelationalText text, @RequestBody DublinCoreMetadata updated) {
    final DublinCoreMetadata metadata = read(text);
    metadata.update(updated);
    jdbcTemplate.update(new StringBuilder("update text_metadata set ")
            .append("text_created = :text_created")
            .append(", text_updated = :text_updated")
            .append(", text_title = :text_title")
            .append(", text_creator = :text_creator")
            .append(", text_subject = :text_subject")
            .append(", text_description = :text_description")
            .append(", text_publisher = :text_publisher")
            .append(", text_contributor = :text_contributor")
            .append(", text_date = :text_date")
            .append(", text_type = :text_type")
            .append(", text_format = :text_format")
            .append(", text_identifier = :text_identifier")
            .append(", text_source = :text_source")
            .append(", text_language = :text_language")
            .append(" where text = :text").toString(), metadata.toSqlParameterSource());
    return metadata;
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, consumes = "application/json", produces = "application/json")
  @ResponseBody
  public RelationalText delete(@PathVariable("id") RelationalText text) {
    jdbcTemplate.getJdbcOperations().update("delete from text_metadata where text = ?", text.getId());
    return text;
  }

  public List<DublinCoreMetadata> read(Iterable<Long> texts) {
    final StringBuilder sql = sql().append(" where md.text").append(SQL.inClause(texts));
    return jdbcTemplate.getJdbcOperations().query(sql.toString(), ROW_MAPPER, Iterables.toArray(texts, Object.class));
  }

  public void index() {
    jdbcTemplate.getJdbcOperations().query(sql().toString(), new RowMapper<Void>() {
      @Override
      public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
        try {
          indexController.update(DublinCoreMetadata.mapMetadataFrom(rs, "md"));
        } catch (IOException e) {
          throw Throwables.propagate(e);
        }
        return null;
      }
    });
  }

  protected StringBuilder sql() {
    return new StringBuilder("select ")
            .append(DublinCoreMetadata.selectMetadataFrom("md"))
            .append(" from text_metadata md");
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.metadataInsert = new SimpleJdbcInsert(dataSource).withTableName("text_metadata");
  }

  private static final RowMapper<DublinCoreMetadata> ROW_MAPPER = new RowMapper<DublinCoreMetadata>() {
    @Override
    public DublinCoreMetadata mapRow(ResultSet rs, int rowNum) throws SQLException {
      return DublinCoreMetadata.mapMetadataFrom(rs, "md");
    }
  };
}
