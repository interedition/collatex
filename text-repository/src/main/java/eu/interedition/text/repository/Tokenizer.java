package eu.interedition.text.repository;

import com.google.common.collect.Lists;
import eu.interedition.text.*;
import eu.interedition.text.event.AnnotationEventListener;
import eu.interedition.text.event.AnnotationEventSource;
import eu.interedition.text.mem.SimpleAnnotation;
import eu.interedition.text.mem.SimpleQName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static eu.interedition.text.query.Criteria.*;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Service
@Transactional
public class Tokenizer {
  public static final QName TOKEN_NAME = new SimpleQName(TextConstants.INTEREDITION_NS_URI, "token");

  private static final Logger LOG = LoggerFactory.getLogger(Tokenizer.class);

  @Autowired
  private AnnotationRepository annotationRepository;

  @Autowired
  private AnnotationEventSource eventSource;

  private int pageSize = 102400;

  private int batchSize = 1024;

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public void tokenize(Text text, TokenizerSettings settings) throws IOException {
    annotationRepository.delete(and(text(text), annotationName(TOKEN_NAME)));
    eventSource.listen(new TokenGeneratingListener(text, settings), pageSize, text, none());
  }

  private class TokenGeneratingListener implements AnnotationEventListener {
    private final TokenizerSettings settings;
    private final Text text;

    private List<Annotation> batch = Lists.newArrayListWithExpectedSize(batchSize);
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
    public void start(long offset, Map<Annotation, Map<QName, String>> annotations) {
      if (settings.startingAnnotationsAreBoundary(text, offset, annotations.keySet())) {
        lastIsTokenBoundary = true;
      }
    }

    @Override
    public void empty(long offset, Map<Annotation, Map<QName, String>> annotations) {
      if (settings.emptyAnnotationsAreBoundary(text, offset, annotations.keySet())) {
        lastIsTokenBoundary = true;
      }
    }

    @Override
    public void end(long offset, Map<Annotation, Map<QName, String>> annotations) {
      if (settings.endingAnnotationsAreBoundary(text, offset, annotations.keySet())) {
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
      emit();
      LOG.debug(text + " has " + tokenCount + " token(s)");
    }

    private void token() {
      if (tokenStart < offset) {
        batch.add(new SimpleAnnotation(text, TOKEN_NAME, new Range(tokenStart, offset)));
        if ((batch.size() % batchSize) == 0) {
          emit();
        }
        tokenCount++;
        tokenStart = Integer.MAX_VALUE;
      }
    }

    private void emit() {
      annotationRepository.create(batch);
      batch.clear();
    }
  }
}
