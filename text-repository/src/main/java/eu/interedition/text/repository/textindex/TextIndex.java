package eu.interedition.text.repository.textindex;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.TextRepository;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Service
@Transactional
public class TextIndex implements InitializingBean, DisposableBean {

    @Autowired
    @Qualifier("dataBaseDirectory")
    private File base;

    @Autowired
    @Qualifier("textRepository")
    private RelationalTextRepository textRepository;

    @Autowired
    @Qualifier("taskExecutor")
    private TaskExecutor taskExecutor;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jt;

    private FSDirectory directory;
    private StandardAnalyzer analyzer;
    private IndexWriter indexWriter;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;

    public List<TextIndexQueryResult> search(TextIndexQuery query) throws IOException, ParseException {
        if (Strings.isNullOrEmpty(query.getQuery())) {
            return Collections.emptyList();
        }

        final Query parsed = new QueryParser(Version.LUCENE_30, "content", analyzer).parse(query.getQuery());
        final int pageSize = query.getPageSize();
        final int offset = query.getPage() * pageSize;

        final Map<Long, TextIndexQueryResult> results = Maps.newLinkedHashMap();
        final IndexSearcher searcher = indexSearcher();
        final TopDocs searchResult = searcher.search(parsed, offset + pageSize);
        for (int rc = offset; rc < searchResult.scoreDocs.length; rc++) {
            final ScoreDoc scoreDocument = searchResult.scoreDocs[rc];
            final Document document = searcher.doc(scoreDocument.doc);

            final TextIndexQueryResult result = new TextIndexQueryResult();
            result.setScore(Math.round(scoreDocument.score * 100));
            results.put(Long.parseLong(document.get("id")), result);
        }

        for (Text text : textRepository.load(results.keySet())) {
            final TextIndexQueryResult result = results.get(((RelationalText) text).getId());
            result.setText(text);
            result.setTextStart(textRepository.read(text, new Range(0, Math.min(1000, text.getLength()))));
        }

        return Lists.newArrayList(results.values());
    }

    public void update(Text text) throws IOException {
        final RelationalText rt = (RelationalText) text;

        textRepository.read(text, new TextRepository.TextReader() {
            @Override
            public void read(Reader content, long contentLength) throws IOException {
                final Document document = new Document();
                document.add(new Field("id", Long.toString(rt.getId()), Field.Store.YES, Field.Index.NOT_ANALYZED));
                document.add(new Field("type", rt.getType().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                document.add(new Field("content_length", Long.toString(rt.getLength()), Field.Store.YES, Field.Index.NOT_ANALYZED));
                document.add(new Field("content", content));

                indexWriter.updateDocument(idTerm(rt), document);
                commit();
            }
        });
    }

    public void delete(Text text) throws IOException {
        indexWriter.deleteDocuments(new TermQuery(idTerm((RelationalText) text)));
        commit();
    }

    protected Term idTerm(RelationalText rt) {
        return new Term("id", Long.toString(rt.getId()));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final File indexBase = new File(base, "text-index");
        Assert.isTrue(indexBase.isDirectory() || indexBase.mkdir(), "Cannot create text index directory " + indexBase);

        this.directory = FSDirectory.open(indexBase);
        this.analyzer = new StandardAnalyzer(Version.LUCENE_30);

        this.indexWriter = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_33, analyzer));
        this.indexReader = IndexReader.open(this.indexWriter, false);

        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    final int numTexts = jt.queryForInt("select count(*) from text_content");
                    final int numDocuments = indexWriter.numDocs();
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
                        jt.query("select " + RelationalTextRepository.selectTextFrom("t") + " from text_content t", new RowMapper<Void>() {
                            @Override
                            public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
                                try {
                                    update(RelationalTextRepository.mapTextFrom(rs, "t"));
                                    return null;
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
