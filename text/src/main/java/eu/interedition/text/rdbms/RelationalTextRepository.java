package eu.interedition.text.rdbms;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Collections.singleton;

import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.SortedMap;
import java.util.SortedSet;

import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work;
import eu.interedition.text.Range;
import eu.interedition.text.Text;
import eu.interedition.text.TextContentReader;
import eu.interedition.text.TextRepository;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;

public class RelationalTextRepository implements TextRepository {

	private SessionFactory sessionFactory;
	private String contentColumn = "content";
	private String textRelation = "lmnl_text";

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setContentColumn(String contentColumn) {
		this.contentColumn = contentColumn;
	}

	public void setTextRelation(String textRelation) {
		this.textRelation = textRelation;
	}

	public void read(Text text, final TextContentReader reader) throws IOException {
		sessionFactory.getCurrentSession().doWork(new TextContentRetrieval<Void>(text) {

			@Override
			protected Void retrieve(Clob content) throws SQLException, IOException {
				Reader contentReader = null;
				try {
					reader.read(contentReader = content.getCharacterStream(), (int) content.length());
				} catch (IOException e) {
					Throwables.propagate(e);
				} finally {
					Closeables.close(contentReader, false);
				}
				return null;
			}
		});
	}

	public String read(Text text, Range range) throws IOException {
		return getOnlyElement(bulkRead(text, Sets.newTreeSet(singleton(range))).values());
	}

	public int length(Text text) throws IOException {
		final TextContentRetrieval<Integer> contentLengthRetrieval = new TextContentRetrieval<Integer>(text) {

			@Override
			protected Integer retrieve(Clob content) throws SQLException, IOException {
				return (int) content.length();
			}
		};

		sessionFactory.getCurrentSession().doWork(contentLengthRetrieval);
		return contentLengthRetrieval.returnValue;
	}

	public SortedMap<Range, String> bulkRead(Text text, final SortedSet<Range> ranges) throws IOException {
		final SortedMap<Range, String> results = Maps.newTreeMap();
		sessionFactory.getCurrentSession().doWork(new TextContentRetrieval<Void>(text) {

			@Override
			protected Void retrieve(Clob content) throws SQLException, IOException {
				for (Range range : ranges) {
					results.put(range, content.getSubString(range.getStart() + 1, range.length()));
				}
				return null;
			}
		});
		return results;
	}

	public void write(final Text text, final Reader contents, final int contentLength) throws IOException {
		sessionFactory.getCurrentSession().doWork(new Work() {

			public void execute(Connection connection) throws SQLException {
				final PreparedStatement updateStmt = connection.prepareStatement("UPDATE " + textRelation + " SET "
						+ contentColumn + " = ? WHERE id = ?");
				try {
					updateStmt.setCharacterStream(1, contents, contentLength);
					updateStmt.setInt(2, ((TextRelation)text).getId());
					updateStmt.executeUpdate();
				} finally {
					updateStmt.close();
				}

			}
		});
	}

	private abstract class TextContentRetrieval<T> implements Work {
		private final Text text;
		private T returnValue;

		public TextContentRetrieval(Text text) {
			this.text = text;
		}

		public void execute(Connection connection) throws SQLException {
			final PreparedStatement contentStmt = connection.prepareStatement("SELECT " + contentColumn + " FROM "
					+ textRelation + " WHERE id = ?");
			try {
				contentStmt.setInt(1, ((TextRelation) text).getId());
				final ResultSet resultSet = contentStmt.executeQuery();
				try {
					if (resultSet.next()) {
						returnValue = retrieve(resultSet.getClob(1));
					}
				} finally {
					resultSet.close();
				}
			} catch (IOException e) {
				Throwables.propagate(e);
			} finally {
				contentStmt.close();
			}
		}

		protected abstract T retrieve(Clob content) throws SQLException, IOException;
	}
}
