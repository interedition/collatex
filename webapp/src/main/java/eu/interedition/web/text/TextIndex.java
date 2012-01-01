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

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import eu.interedition.text.TextConsumer;
import eu.interedition.text.rdbms.RelationalText;
import eu.interedition.text.rdbms.RelationalTextRepository;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Transactional
public class TextIndex implements InitializingBean, DisposableBean {

  private final File base;

  @Autowired
  private TextService textService;

  @Autowired
  private RelationalTextRepository textRepository;

  @Autowired
  private ScheduledExecutorService taskExecutor;

  @Autowired
  private PlatformTransactionManager transactionManager;

  @Autowired
  private JdbcTemplate jt;

  private FSDirectory directory;
  private StandardAnalyzer analyzer;
  private IndexWriter indexWriter;
  private IndexReader indexReader;
  private IndexSearcher indexSearcher;

  public TextIndex(File base) {
    this.base = base;
  }

  public List<TextIndexQueryResult> search(TextIndexQuery query) throws IOException, ParseException {
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

    final List<TextIndexQueryResult> results = Lists.newArrayListWithExpectedSize(scores.size());
    for (TextMetadata metadata : textService.load(scores.keySet())) {
      results.add(new TextIndexQueryResult(metadata, scores.get(metadata.getText().getId())));
    }
    return results;
  }

  public void update(final TextMetadata metadata) throws IOException {
    final RelationalText text = metadata.getText();
    textRepository.read(text, new TextConsumer() {
      @Override
      public void read(Reader content, long contentLength) throws IOException {
        final Document document = new Document();
        document.add(new Field("id", Long.toString(text.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED));
        document.add(new Field("type", text.getType().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
        document.add(new Field("content_length", Long.toString(text.getLength()), Field.Store.YES, Field.Index.NOT_ANALYZED));
        document.add(new Field("content", content));

        metadata.addTo(document);

        indexWriter.updateDocument(idTerm(metadata), document);
        commit();
      }
    });
  }

  public void delete(TextMetadata metadata) throws IOException {
    indexWriter.deleteDocuments(new TermQuery(idTerm(metadata)));
    commit();
  }

  protected Term idTerm(TextMetadata metadata) {
    return new Term("id", Long.toString(metadata.getText().getId()));
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.directory = FSDirectory.open(base);
    this.analyzer = new StandardAnalyzer(Version.LUCENE_30);

    this.indexWriter = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_33, analyzer));
    this.indexReader = IndexReader.open(this.indexWriter, false);

    new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          final long numTexts = jt.queryForLong("select count(*) from text_metadata");
          final long numDocuments = indexWriter.numDocs();
          if (numTexts != numDocuments) {
            taskExecutor.execute(new IndexingTask());
          }
        } catch (IOException e) {
          throw Throwables.propagate(e);
        }
      }
    });
  }

  @Override
  public void destroy() throws Exception {
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

  private class IndexingTask implements Runnable {

    @Override
    public void run() {
      new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
          try {
            indexWriter.deleteAll();

            textService.scroll(new TextService.TextScroller() {
              @Override
              public void text(TextMetadata text) {
                try {
                  update(text);
                } catch (IOException e) {
                  throw Throwables.propagate(e);
                }
              }
            });

            commit();
          } catch (IOException e) {
            throw Throwables.propagate(e);
          }
        }
      });
    }
  }
}
