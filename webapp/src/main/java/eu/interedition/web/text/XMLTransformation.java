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

import com.google.common.collect.Lists;
import eu.interedition.text.Name;
import eu.interedition.text.rdbms.RelationalName;
import eu.interedition.text.rdbms.RelationalNameRegistry;
import eu.interedition.text.util.SQL;
import eu.interedition.text.util.SimpleXMLTransformerConfiguration;
import eu.interedition.text.xml.XMLTransformerModule;
import eu.interedition.text.xml.module.CLIXAnnotationXMLTransformerModule;
import eu.interedition.text.xml.module.DefaultAnnotationXMLTransformerModule;
import eu.interedition.text.xml.module.LineElementXMLTransformerModule;
import eu.interedition.text.xml.module.NotableCharacterXMLTransformerModule;
import eu.interedition.text.xml.module.TEIAwareAnnotationXMLTransformerModule;
import eu.interedition.text.xml.module.TextXMLTransformerModule;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLTransformation extends SimpleXMLTransformerConfiguration {
  protected long id;
  protected String name;
  protected String description;
  protected boolean transformTEI = true;
  protected boolean removeEmpty = false;
  protected List<XMLTransformationRule> rules;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isTransformTEI() {
    return transformTEI;
  }

  public void setTransformTEI(boolean transformTEI) {
    this.transformTEI = transformTEI;
  }

  public boolean isRemoveEmpty() {
    return removeEmpty;
  }

  public void setRemoveEmpty(boolean removeEmpty) {
    this.removeEmpty = removeEmpty;
  }

  public List<XMLTransformationRule> getRules() {
    return rules;
  }

  public void setRules(List<XMLTransformationRule> rules) {
    this.rules = rules;

    excluded.clear();
    included.clear();
    containerElements.clear();
    lineElements.clear();
    notableElements.clear();

    for (XMLTransformationRule rule : rules) {
      final Name name = rule.getName();
      if (rule.isExcluded()) {
        excluded.add(name);
      }
      if (rule.isIncluded()) {
        included.add(name);
      }
      if (rule.isContainerElement()) {
        containerElements.add(name);
      }
      if (rule.isLineElement()) {
        lineElements.add(name);
      }
      if (rule.isNotable()) {
        notableElements.add(name);
      }
    }
  }

  @JsonIgnore
  @Override
  public List<XMLTransformerModule> getModules() {
    final List<XMLTransformerModule> modules = Lists.<XMLTransformerModule>newArrayList(
            new LineElementXMLTransformerModule(),
            new NotableCharacterXMLTransformerModule(),
            new TextXMLTransformerModule(),
            new DefaultAnnotationXMLTransformerModule(1000, true),
            new CLIXAnnotationXMLTransformerModule(1000));
    if (transformTEI) {
      modules.add(new TEIAwareAnnotationXMLTransformerModule(1000));
    }
    return modules;
  }

  @JsonIgnore
  @Override
  public int getTextBufferSize() {
    return super.getTextBufferSize();
  }

  @JsonIgnore
  @Override
  public void setTextBufferSize(int textBufferSize) {
    super.setTextBufferSize(textBufferSize);
  }

  public XMLTransformation update(XMLTransformation updated) {
    setName(updated.getName());
    setDescription(updated.getDescription());
    setRemoveEmpty(updated.isRemoveEmpty());
    setTransformTEI(updated.isTransformTEI());
    setCompressingWhitespace(updated.isCompressingWhitespace());
    setNotableCharacter(updated.getNotableCharacter());
    setRemoveLeadingWhitespace(updated.isRemoveLeadingWhitespace());
    setRules(updated.getRules());
    return this;
  }


  public XMLTransformation save(JdbcTemplate jdbcTemplate, SimpleJdbcInsert xtInsert, SimpleJdbcInsert xtrInsert, RelationalNameRegistry nameRegistry) {
    if (id == 0) {
      id = xtInsert.executeAndReturnKey(new MapSqlParameterSource()
              .addValue("name", name)
              .addValue("description", description)
              .addValue("transform_tei", transformTEI)
              .addValue("compress_space", compressingWhitespace)
              .addValue("remove_empty", removeEmpty)
              .addValue("notable_char", Character.toString(notableCharacter))).longValue();
    } else {
      jdbcTemplate.update("update xml_transform" +
              " set name = ?, description = ?, transform_tei = ?, compress_space = ?, remove_empty = ?, notable_char = ?" +
              " where id = ?", name, description, transformTEI, compressingWhitespace, removeEmpty, Character.toString(notableCharacter), id);
      jdbcTemplate.update("delete from xml_transform_rule where config = ?", id);
    }

    final List<MapSqlParameterSource> ruleBatch = Lists.newArrayListWithExpectedSize(rules.size());
    for (XMLTransformationRule rule : rules) {
      if (rule.isEmpty()) {
        continue;
      }
      ruleBatch.add(new MapSqlParameterSource()
              .addValue("config", id)
              .addValue("name", ((RelationalName) nameRegistry.get(rule.getName())).getId())
              .addValue("is_line", rule.isLineElement())
              .addValue("is_container", rule.isContainerElement())
              .addValue("is_excluded", rule.isExcluded())
              .addValue("is_included", rule.isIncluded())
              .addValue("is_notable", rule.isNotable()));
    }
    xtrInsert.executeBatch(ruleBatch.toArray(new MapSqlParameterSource[ruleBatch.size()]));

    return this;
  }


  public static List<XMLTransformation> all(JdbcTemplate jdbcTemplate) {
    return jdbcTemplate.query(new StringBuilder("select ")
            .append(select("xt")).append(" from xml_transform xt order by xt.name").toString(), ROW_MAPPER);
  }

  public static String select(String tableName) {
    return SQL.select(tableName, "id", "name", "description", "transform_tei", "compress_space", "remove_empty", "notable_char");
  }

  public static final RowMapper<XMLTransformation> ROW_MAPPER = new RowMapper<XMLTransformation>() {
    @Override
    public XMLTransformation mapRow(ResultSet rs, int rowNum) throws SQLException {
      final XMLTransformation xt = new XMLTransformation();
      xt.setId(rs.getLong("xt_id"));
      xt.setName(rs.getString("xt_name"));
      xt.setTransformTEI(rs.getBoolean("xt_transform_tei"));
      xt.setCompressingWhitespace(rs.getBoolean("xt_compress_space"));
      xt.setRemoveEmpty(rs.getBoolean("xt_remove_empty"));
      xt.setNotableCharacter(rs.getString("xt_notable_char").charAt(0));
      return xt;
    }
  };
}
