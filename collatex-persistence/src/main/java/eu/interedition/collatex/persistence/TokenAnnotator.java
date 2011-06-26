package eu.interedition.collatex.persistence;

import com.google.common.base.Preconditions;
import eu.interedition.text.*;
import eu.interedition.text.event.TextEventGenerator;
import eu.interedition.text.event.TextEventHandler;
import eu.interedition.text.rdbms.RelationalAnnotationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Service
@Transactional
public class TokenAnnotator {
  public static final QName TOKEN_NAME = new QNameImpl(Annotation.INTEREDITION_NS_URI, "token");

  private static final Logger LOG = LoggerFactory.getLogger(TokenAnnotator.class);

  @Autowired
  private RelationalAnnotationFactory annotationFactory;

  @Autowired
  private AnnotationRepository annotationRepository;

  @Autowired
  private TextEventGenerator eventGenerator;

  private int pageSize = 102400;

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public void tokenize(final Witness witness) throws IOException {
    final Text text = witness.getText();
    Preconditions.checkNotNull(text);

    for (Annotation token : annotationRepository.find(text, TOKEN_NAME)) {
      annotationFactory.delete(token);
    }

    eventGenerator.generate(new AnnotatingTextEventProcessor(witness), text, null, pageSize);
  }

  private class AnnotatingTextEventProcessor implements TextEventHandler {
    private final Witness witness;
    private final Text text;

    int numTokens = 0;
    int offset = 0;
    int start = Integer.MAX_VALUE;
    boolean prevIsTokenContent = false;

    private AnnotatingTextEventProcessor(Witness witness) {
      this.witness = witness;
      this.text = witness.getText();
    }

    @Override
    public void start() {
      LOG.debug("Tokenizing " + witness);
    }

    @Override
    public void start(int offset, Set<Annotation> annotations) {
    }

    @Override
    public void empty(int offset, Set<Annotation> annotations) {
    }

    @Override
    public void end(int offset, Set<Annotation> annotations) {
    }

    @Override
    public void text(Range r, char[] content) {
      for (char c : content) {
        if (isTokenBoundary(c)) {
          prevIsTokenContent = false;
        } else {
          if (!prevIsTokenContent && start < offset) {
            createToken(text, start, offset);
            numTokens++;
            start = Integer.MAX_VALUE;
          }
          if (start > offset) {
            start = offset;
          }
          prevIsTokenContent = true;
        }

        offset++;
      }
    }

    @Override
    public void end() {
      if (start < offset) {
        createToken(text, start, offset);
        numTokens++;
      }

      LOG.debug(witness + " has " + numTokens + " token(s)");
    }

    protected void createToken(Text text, int start, int end) {
      annotationFactory.create(text, TOKEN_NAME, new Range(start, end));
    }
  }

  protected boolean isTokenBoundary(char c) {
    return Character.isWhitespace(c);
  }
}
