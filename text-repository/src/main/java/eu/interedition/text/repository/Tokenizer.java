package eu.interedition.text.repository;

import eu.interedition.text.*;
import eu.interedition.text.event.AnnotationEventSource;
import eu.interedition.text.event.AnnotationEventListener;
import eu.interedition.text.mem.SimpleQName;
import eu.interedition.text.predicate.TextPredicate;
import eu.interedition.text.predicate.AnnotationNamePredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Service
@Transactional
public class Tokenizer {
  public static final QName TOKEN_NAME = new SimpleQName(Annotation.INTEREDITION_NS_URI, "token");

  private static final Logger LOG = LoggerFactory.getLogger(Tokenizer.class);

  @Autowired
  private AnnotationRepository annotationRepository;

  @Autowired
  private AnnotationEventSource eventSource;

  private int pageSize = 102400;

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public void tokenize(Text text, TokenizerSettings settings) throws IOException {
    annotationRepository.delete(new TextPredicate(text), new AnnotationNamePredicate(TOKEN_NAME));
    eventSource.listen(new TokenGeneratingListener(text, settings), pageSize, new TextPredicate(text));
  }

  private class TokenGeneratingListener implements AnnotationEventListener {
    private final TokenizerSettings settings;
    private final Text text;

    private boolean lastIsTokenBoundary = true;
    private int offset = 0;
    private int tokenStart = Integer.MAX_VALUE;
    private int tokenCount = 0;

    private TokenGeneratingListener(Text text, TokenizerSettings settings) {
      this.settings = settings;
      this.text = text;
    }

    @Override
    public void start() {
      LOG.debug("Tokenizing " + text);
    }

    @Override
    public void start(int offset, Set<Annotation> annotations) {
      if (settings.startingAnnotationsAreBoundary(text, offset, annotations)) {
        lastIsTokenBoundary = true;
      }
    }

    @Override
    public void empty(int offset, Set<Annotation> annotations) {
      if (settings.emptyAnnotationsAreBoundary(text, offset, annotations)) {
        lastIsTokenBoundary = true;
      }
    }

    @Override
    public void end(int offset, Set<Annotation> annotations) {
      if (settings.endingAnnotationsAreBoundary(text, offset, annotations)) {
        lastIsTokenBoundary = true;
      }
    }

    @Override
    public void text(Range r, String content) {
      for (char c : content.toCharArray()) {
        if (settings.isBoundary(text, offset, c)) {
          lastIsTokenBoundary = true;
        } else {
          if (lastIsTokenBoundary) {
            token();
          }
          if (tokenStart > offset) {
            tokenStart = offset;
          }
          lastIsTokenBoundary = false;
        }

        offset++;
      }
    }

    @Override
    public void end() {
      token();
      LOG.debug(text + " has " + tokenCount + " token(s)");
    }

    private void token() {
      if (tokenStart < offset) {
        annotationRepository.create(text, TOKEN_NAME, new Range(tokenStart, offset));
        tokenCount++;
        tokenStart = Integer.MAX_VALUE;
      }
    }
  }
}
