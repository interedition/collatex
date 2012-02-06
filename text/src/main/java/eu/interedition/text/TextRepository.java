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

import javax.xml.stream.*;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.Reader;
import java.util.SortedMap;
import java.util.SortedSet;

public interface TextRepository {

  Text create(Annotation layer, Text.Type type);

  Text create(Annotation layer, Reader content) throws IOException;

  Text create(Annotation layer, XMLStreamReader xml) throws IOException, XMLStreamException;

  void delete(Text text);

  void read(Text text, XMLStreamWriter xml) throws IOException, XMLStreamException;

  void read(Text text, TextConsumer consumer) throws IOException;

  void read(Text text, Range range, TextConsumer consumer) throws IOException;

  String read(Text text, Range range) throws IOException;

  SortedMap<Range, String> bulkRead(Text text, SortedSet<Range> ranges) throws IOException;

  Text write(Text text, Reader contents) throws IOException;

  Text write(Text text, Reader contents, long contentLength) throws IOException;
}
