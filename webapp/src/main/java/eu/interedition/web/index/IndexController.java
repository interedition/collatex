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
package eu.interedition.web.index;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import eu.interedition.text.rdbms.RelationalText;
import eu.interedition.text.rdbms.RelationalTextRepository;
import eu.interedition.web.metadata.DublinCoreMetadata;
import eu.interedition.web.metadata.MetadataController;
import eu.interedition.web.text.TextController;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Controller
@RequestMapping("/index")
public class IndexController implements InitializingBean, DisposableBean {

  @Autowired
  @Qualifier("dataDirectory")
  private File dataDirectory;

  @Autowired
  private MetadataController metadataController;

  @Autowired
  private RelationalTextRepository textRepository;

  private FSDirectory directory;
  private StandardAnalyzer analyzer;
  private IndexWriter indexWriter;
  private IndexReader indexReader;
  private IndexSearcher indexSearcher;

  @RequestMapping(produces = "text/html")
  public ModelAndView search(@ModelAttribute("query") IndexQuery query) throws IOException, ParseException {
    final List<IndexQueryResult> searchResults = query(query);
    return (searchResults.size() == 1) ?
            new ModelAndView(TextController.redirectTo(searchResults.get(0).getText())) :
            new ModelAndView("search", "results", searchResults);
  }

  @RequestMapping(produces = "application/json")
  @ResponseBody
  public List<IndexQueryResult> query(@ModelAttribute("query") IndexQuery query) throws IOException, ParseException {
    if (Strings.isNullOrEmpty(query.getQuery())) {
      return Collections.emptyList();
    }

    final Query parsed = new QueryParser(Version.LUCENE_30, "content", analyzer).parse(query.getQuery());
    final int pageSize = query.getPageSize();
    final int offset = query.getPage() * pageSize;

    final Map<Long, Integer> scores = Maps.newLinkedHashMap();
    final IndexSearcher searcher = indexSearcher();
    final TopDocs searchResult = searcher.search(parsed, offset + pageSize);
    for (int rc = offset; rc < searchResult.scoreDocs.length; rc++) {
      final ScoreDoc scoreDocument = searchResult.scoreDocs[rc];
      final Document document = searcher.doc(scoreDocument.doc);
      scores.put(Long.parseLong(document.get("id")), Math.round(scoreDocument.score * 100));
    }

    final List<IndexQueryResult> results = Lists.newArrayListWithExpectedSize(scores.size());
    for (DublinCoreMetadata metadata : metadataController.read(scores.keySet())) {
      results.add(new IndexQueryResult(metadata, scores.get(metadata.getText())));
    }
    return results;
  }

  public void update(DublinCoreMetadata metadata) throws IOException {
    final RelationalText text = textRepository.read(metadata.getText());
    Reader textReader = null;
    try {
      final Document document = new Document();
      document.add(new Field("id", Long.toString(text.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED));
      document.add(new Field("type", text.getType().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
      document.add(new Field("content_length", Long.toString(text.getLength()), Field.Store.YES, Field.Index.NOT_ANALYZED));
      document.add(new Field("content", textReader = textRepository.read(text).getInput()));

      metadata.addTo(document);

      indexWriter.updateDocument(idTerm(metadata), document);
      commit();

    } finally {
      Closeables.close(textReader, false);
    }
  }

  public void delete(DublinCoreMetadata metadata) throws IOException {
    indexWriter.deleteDocuments(new TermQuery(idTerm(metadata)));
    commit();
  }

  protected Term idTerm(DublinCoreMetadata metadata) {
    return new Term("id", Long.toString(metadata.getText()));
  }

  public void index() throws IOException {
    indexWriter.deleteAll();
    metadataController.index();
    commit();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    final File indexHome = new File(dataDirectory, "index");
    Assert.isTrue(indexHome.isDirectory() || indexHome.mkdirs(),//
            "Fulltext index directory '" + indexHome + "' does not exist and could not be created");

    this.directory = FSDirectory.open(indexHome);
    this.analyzer = new StandardAnalyzer(Version.LUCENE_30);

    this.indexWriter = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_33, analyzer));
    this.indexReader = IndexReader.open(this.indexWriter, false);
  }

  @Override
  public void destroy() throws Exception {
    Closeables.close(this.indexReader, false);
    Closeables.close(this.indexWriter, false);
    Closeables.close(this.directory, false);
  }

  protected synchronized IndexSearcher indexSearcher() throws IOException {
    if (this.indexSearcher == null) {
      this.indexSearcher = new IndexSearcher(indexReader.reopen());
    }
    return indexSearcher;
  }

  protected synchronized void commit() throws IOException {
    this.indexWriter.commit();

    Closeables.closeQuietly(this.indexSearcher);
    this.indexSearcher = null;
  }
}
