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

import com.google.common.base.Function;
import com.google.common.io.InputSupplier;
import eu.interedition.text.query.Criterion;

import javax.xml.stream.*;
import java.io.IOException;
import java.io.Reader;
import java.util.SortedMap;
import java.util.SortedSet;

public interface TextRepository {

  Text create(Annotation layer, Text.Type type);

  Text create(Annotation layer, Reader content) throws IOException;

  Text create(Annotation layer, XMLStreamReader xml) throws IOException, XMLStreamException;

  Iterable<Annotation> create(Annotation... annotations);

  Iterable<Annotation> create(Iterable<Annotation> annotations);

  void delete(Text text);

  void delete(Iterable<Annotation> annotations);

  void delete(Annotation... annotations);

  void delete(Criterion criterion);

  void read(Text text, XMLStreamWriter xml) throws IOException, XMLStreamException;

  InputSupplier<Reader> read(Text text) throws IOException;

  InputSupplier<Reader> read(Text text, Range range) throws IOException;

  void read(Text text, Criterion criterion, TextListener listener) throws IOException;

  void read(Text text, Criterion criterion, int pageSize, TextListener listener) throws IOException;

  SortedMap<Range, String> read(Text text, SortedSet<Range> ranges) throws IOException;

  Text write(Text text, Reader contents) throws IOException;

  Text write(Text text, Reader contents, long contentLength) throws IOException;

  Iterable<Annotation> find(Criterion criterion);

  void transform(Criterion criterion, Text to, Function<Annotation, Annotation> transform);

  Iterable<Annotation> transform(Iterable<Annotation> annotations, Text to, Function<Annotation, Annotation> transform);
}
