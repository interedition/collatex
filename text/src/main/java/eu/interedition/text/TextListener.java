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
package eu.interedition.text;

import eu.interedition.text.Annotation;
import eu.interedition.text.Name;
import eu.interedition.text.Range;

import java.util.Map;


public interface TextListener {

  void start(long contentLength);

  void start(long offset, Iterable<Annotation> annotations);

  void end(long offset, Iterable<Annotation> annotations);

  void text(Range r, String text);

  void end();
}
