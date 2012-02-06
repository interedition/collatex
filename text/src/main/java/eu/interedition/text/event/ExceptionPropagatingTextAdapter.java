/*
 * #%L
 * Text: A text model with range-based markup via standoff annotations.
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
package eu.interedition.text.event;

import com.google.common.base.Throwables;
import eu.interedition.text.Annotation;
import eu.interedition.text.Range;
import eu.interedition.text.TextListener;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ExceptionPropagatingTextAdapter implements TextListener {
  public void start(long contentLength) {
    try {
      doStart(contentLength);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public void start(long offset, Iterable<Annotation> annotations) {
    try {
      doStart(offset, annotations);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public void end(long offset, Iterable<Annotation> annotations) {
    try {
      doEnd(offset, annotations);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public void text(Range r, String text) {
    try {
      doText(r, text);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  public void end() {
    try {
      doEnd();
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  protected void doStart(long contentLength) throws Exception {
  }

  protected void doStart(long offset, Iterable<Annotation> annotations) throws Exception {
  }

  protected void doEnd(long offset, Iterable<Annotation> annotations) throws Exception {
  }

  protected void doText(Range r, String text) throws Exception {
  }

  protected void doEnd() throws Exception {
  }
}
