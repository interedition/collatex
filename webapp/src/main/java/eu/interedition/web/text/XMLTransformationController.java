package eu.interedition.web.text;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import eu.interedition.text.Text;
import eu.interedition.text.TextConstants;
import eu.interedition.text.TextRepository;
import eu.interedition.text.mem.SimpleName;
import eu.interedition.text.query.Criteria;
import eu.interedition.text.rdbms.RelationalName;
import eu.interedition.text.rdbms.RelationalNameRegistry;
import eu.interedition.text.rdbms.RelationalText;
import eu.interedition.text.util.SQL;
import eu.interedition.text.xml.XML;
import eu.interedition.text.xml.XMLTransformer;
import eu.interedition.web.metadata.DublinCoreMetadata;
import eu.interedition.web.metadata.MetadataController;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Controller
@RequestMapping("/xml/transform")
public class XMLTransformationController implements InitializingBean {
  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private RelationalNameRegistry nameRegistry;

  @Autowired
  private TextRepository repository;

  @Autowired
  private MetadataController metadataController;

  private XMLInputFactory xmlInputFactory = XML.createXMLInputFactory();

  private SimpleJdbcInsert xtInsert;
  private SimpleJdbcInsert xtrInsert;

  @RequestMapping(produces = "application/json")
  @ResponseBody
  public List<XMLTransformation> transformations() {
    return XMLTransformation.all(jdbcTemplate);
  }

  @RequestMapping(produces = "text/html")
  public String index() {
    return "xml_transform";
  }

  @RequestMapping(value = "{id}", produces = "application/json")
  @ResponseBody
  public XMLTransformation read(@PathVariable("id") XMLTransformation xt) {
    return xt;
  }

  @RequestMapping(value = "{id}", produces = "text/html")
  @ResponseBody
  public ModelAndView transformation(@PathVariable("id") XMLTransformation xt) {
    return new ModelAndView("xml_transform", "xt", xt);
  }

  @RequestMapping(method = RequestMethod.POST, produces = "application/json")
  @ResponseBody
  public XMLTransformation create(@RequestBody XMLTransformation xt) {
    return xt.save(jdbcTemplate, xtInsert, xtrInsert, nameRegistry);
  }

  @RequestMapping(value = "{id}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json")
  @ResponseBody
  public XMLTransformation update(@PathVariable("id") XMLTransformation xt, @RequestBody XMLTransformation updated) {
    return xt.update(updated).save(jdbcTemplate, xtInsert, xtrInsert, nameRegistry);
  }

  @RequestMapping(value = "{id}", method = RequestMethod.POST, consumes = "application/xml", produces = "application/json")
  @ResponseBody
  public Text transform(@PathVariable("id") XMLTransformation xt, InputStream xmlStream) throws XMLStreamException, IOException {
    XMLStreamReader xmlReader = null;
    try {
      final Text xmlText = repository.create(null, xmlReader = xmlInputFactory.createXMLStreamReader(xmlStream));

      final DublinCoreMetadata metadata = new DublinCoreMetadata(DateTime.now()).update(repository, xmlText);
      metadataController.create((RelationalText) xmlText, metadata);

      final Text text = new XMLTransformer(repository, xt).transform(xmlText);
      metadataController.create((RelationalText) text, metadata);

      if (xt.isRemoveEmpty()) {
        repository.delete(Criteria.and(Criteria.text(text), Criteria.rangeLength(0)));
      }
      return text;
    } finally {
      XML.closeQuietly(xmlReader);
      Closeables.close(xmlStream, false);
    }
  }

  @RequestMapping(value = "{id}", method = RequestMethod.POST, consumes = "multipart/form-data", produces = "application/json")
  @ResponseBody
  public Text transform(@PathVariable("id") XMLTransformation xt, @RequestParam("xml") MultipartFile xml) throws XMLStreamException, IOException {
    return transform(xt, xml.getInputStream());
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    xtInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("xml_transform").usingGeneratedKeyColumns("id");
    xtrInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("xml_transform_rule");

    if (jdbcTemplate.queryForInt("select count(*) from xml_transform") == 0) {
      final List<XMLTransformationRule> teiDefaultRules = Lists.newArrayList();
      teiDefaultRules.add(new XMLTransformationRule(new SimpleName(TextConstants.TEI_NS, "teiHeader"), false, false, false, true, false));
      teiDefaultRules.add(new XMLTransformationRule(new SimpleName(TextConstants.TEI_NS, "div"), true, false, false, false, false));
      teiDefaultRules.add(new XMLTransformationRule(new SimpleName(TextConstants.TEI_NS, "p"), true, false, false, false, false));
      teiDefaultRules.add(new XMLTransformationRule(new SimpleName(TextConstants.TEI_NS, "l"), true, false, false, false, false));
      teiDefaultRules.add(new XMLTransformationRule(new SimpleName(TextConstants.TEI_NS, "head"), true, false, false, false, false));

      final XMLTransformation teiDefault = new XMLTransformation();
      teiDefault.setName("tei");
      teiDefault.setRemoveEmpty(true);
      teiDefault.setTransformTEI(true);
      teiDefault.setRules(teiDefaultRules);

      teiDefault.save(jdbcTemplate, xtInsert, xtrInsert, nameRegistry);
    }
  }
}
