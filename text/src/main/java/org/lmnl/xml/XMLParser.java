package org.lmnl.xml;

import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Stack;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.lmnl.*;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.io.FileBackedOutputStream;

public abstract class XMLParser {
	public static final QName OFFSET_DELTA_NAME = new QNameImpl(Annotation.LMNL_NS_URI, "offset");
	public static final QName NODE_PATH_NAME = new QNameImpl(Annotation.LMNL_NS_URI, "xmlNode");

	private final TransformerFactory transformerFactory;
	private final XMLInputFactory xmlInputFactory;

	private TextRepository textRepository;

	private Charset charset = Charset.forName("UTF-8");
	private boolean removeLeadingWhitespace = true;
	private int textBufferSize = 100000;
	private int xmlEventBatchSize = 1000;

	public XMLParser() {
		transformerFactory = TransformerFactory.newInstance();
		xmlInputFactory = XMLInputFactory.newInstance();
		xmlInputFactory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
		xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
		xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
	}

	public void setTextRepository(TextRepository textRepository) {
		this.textRepository = textRepository;
	}

	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	public void setRemoveLeadingWhitespace(boolean removeLeadingWhitespace) {
		this.removeLeadingWhitespace = removeLeadingWhitespace;
	}

	public void setTextBufferSize(int textBufferSize) {
		this.textBufferSize = textBufferSize;
	}

	public void setXmlEventBatchSize(int xmlEventBatchSize) {
		this.xmlEventBatchSize = xmlEventBatchSize;
	}

	public void load(Text text, Source xml) throws IOException, TransformerException {
		File sourceContents = File.createTempFile(getClass().getName(), ".xml");
		sourceContents.deleteOnExit();

		Reader sourceContentReader = null;

		try {
			final Transformer serializer = transformerFactory.newTransformer();
			serializer.setOutputProperty(OutputKeys.METHOD, "xml");
			serializer.setOutputProperty(OutputKeys.ENCODING, charset.name());
			serializer.setOutputProperty(OutputKeys.INDENT, "no");
			serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			serializer.transform(xml, new StreamResult(sourceContents));

			sourceContentReader = new InputStreamReader(new FileInputStream(sourceContents), charset);
			final int sourceContentLength = contentLength(sourceContentReader);
			sourceContentReader.close();

			sourceContentReader = new InputStreamReader(new FileInputStream(sourceContents), charset);
			updateText(text, sourceContentReader, sourceContentLength);
		} finally {
			Closeables.close(sourceContentReader, false);
			sourceContents.delete();
		}
	}

	public void parse(Text source, Text target, XMLParserConfiguration configuration)
			throws IOException, XMLStreamException {
		Session session = null;
		try {
			session = new Session(source, target, configuration);
			textRepository.read(source, session);
		} catch (Throwable t) {
			Throwables.propagateIfInstanceOf(t, IOException.class);
			Throwables.propagateIfInstanceOf(Throwables.getRootCause(t), XMLStreamException.class);
			Throwables.propagate(t);
		} finally {
			if (session != null) {
				session.dispose();
			}

		}
	}

	protected void updateText(Text text, Reader reader, int contentLength) throws IOException {
		textRepository.write(text, reader, contentLength);
	}

	protected abstract Annotation startAnnotation(Session session, QName name, Map<QName, String> attrs, int start,
			Iterable<Integer> nodePath);

	protected abstract void endAnnotation(Annotation annotation, int end);

	protected abstract void newOffsetDelta(Session session, Range textRange, Range sourceRange);

	protected void newXMLEventBatch() {
	}

	private int contentLength(Reader reader) throws IOException {
		int contentLength = 0;
		while (reader.read() >= 0) {
			contentLength++;
		}
		return contentLength;
	}

	protected class Session implements TextContentReader {
		public final Text source;
		public final Text target;
		public final XMLParserConfiguration configuration;

		protected final Stack<Annotation> elementContext = new Stack<Annotation>();
		protected final Stack<Boolean> spacePreservationContext = new Stack<Boolean>();
		protected final Stack<Boolean> inclusionContext = new Stack<Boolean>();
		protected final Stack<Integer> nodePath = new Stack<Integer>();
		protected final FileBackedOutputStream textBuffer = new FileBackedOutputStream(textBufferSize);

		protected int textOffset = 0;
		protected int textStartOffset = -1;
		protected Range lastDeltaTextRange = Range.NULL;
		protected Range lastDeltaSourceRange = Range.NULL;

		protected char notableCharacter;
		protected char lastChar = (removeLeadingWhitespace ? ' ' : 0);
		private XMLStreamReader reader;

		protected Session(Text source, Text target, XMLParserConfiguration configuration) {
			this.source = source;
			this.target = target;
			this.configuration = configuration;
			this.notableCharacter = configuration.getNotableCharacter();
			this.nodePath.push(0);
		}

		protected Annotation startAnnotation(QName name, Map<QName, String> attributes) throws IOException {
			checkOffsetDelta(lastDeltaSourceRange.getEnd());

			final boolean lineElement = configuration.isLineElement(name);
			final boolean notable = configuration.isNotable(name);
			if (lineElement || notable) {
				if (lineElement && textOffset > 0) {
					insertSpecialChar('\n');
				}
				if (notable) {
					insertSpecialChar(notableCharacter);
				}
			}

			final Annotation annotation = XMLParser.this.startAnnotation(this, name, attributes, textOffset, nodePath);

			elementContext.push(annotation);

			final boolean parentIncluded = (inclusionContext.isEmpty() ? true : inclusionContext.peek());
			inclusionContext.push(parentIncluded ? !configuration.excluded(name) : configuration.included(name));

			spacePreservationContext.push(spacePreservationContext.isEmpty() ? false : spacePreservationContext.peek());
			for (Map.Entry<QName, String> attr : attributes.entrySet()) {
				if (QNameImpl.XML_SPACE.equals(attr.getKey())) {
					spacePreservationContext.pop();
					spacePreservationContext.push("preserve".equalsIgnoreCase(attr.getValue()));
				}
			}
			nodePath.push(0);
			return annotation;
		}

		protected void insertSpecialChar(char specialChar) throws IOException {
			textBuffer.write(Character.toString(lastChar = specialChar).getBytes(charset));

			final int sourceOffset = lastDeltaSourceRange.getEnd();
			newOffsetDelta(this, lastDeltaTextRange = new Range(textOffset, ++textOffset),//
					lastDeltaSourceRange = new Range(sourceOffset, sourceOffset));
		}

		protected void endAnnotation() throws IOException {
			nodePath.pop();
			spacePreservationContext.pop();
			inclusionContext.pop();
			XMLParser.this.endAnnotation(elementContext.pop(), textOffset);
		}

		protected void nextSibling() {
			nodePath.push(nodePath.pop() + 1);
		}

		protected void text() throws IOException {
			if (textStartOffset < 0) {
				nextSibling();
				textStartOffset = textOffset;
			}

			if (!inclusionContext.isEmpty() && !inclusionContext.peek()) {
				return;
			}

			final boolean preserveSpace = !spacePreservationContext.isEmpty() && spacePreservationContext.peek();
			if (!preserveSpace && !elementContext.isEmpty()
					&& configuration.isContainerElement(elementContext.peek().getName())) {
				return;
			}

			final int sourceOffset = reader.getLocation().getCharacterOffset();
			checkOffsetDelta(sourceOffset);

			final String text = reader.getText();
			final int textLength = text.length();
			for (int cc = 0; cc < textLength; cc++) {
				final char currentChar = text.charAt(cc);
				if (!preserveSpace && configuration.isCompressingWhitespace() && Character.isWhitespace(lastChar)
						&& Character.isWhitespace(currentChar)) {
					continue;
				}
				textBuffer.write(Character.toString(lastChar = currentChar).getBytes(charset));
				textOffset++;
			}
			
			checkOffsetDelta(sourceOffset + textLength);
		}

		protected void end() {
			checkOffsetDelta(reader.getLocation().getCharacterOffset());
		}

		protected void checkOffsetDelta(int sourceOffset) {
			if (lastDeltaSourceRange.getEnd() < sourceOffset || lastDeltaTextRange.getEnd() < textOffset) {
				newOffsetDelta(this, lastDeltaTextRange = new Range(lastDeltaTextRange.getEnd(), textOffset),//
						lastDeltaSourceRange = new Range(lastDeltaSourceRange.getEnd(), sourceOffset));
			}
		}

		protected void writeText() {
			if (textStartOffset >= 0 && textOffset > textStartOffset) {
				Annotation text = XMLParser.this.startAnnotation(this, QNameImpl.TEXT_QNAME,
						Maps.<QName, String> newHashMap(), textStartOffset, nodePath);
				XMLParser.this.endAnnotation(text, textOffset);
			}
			textStartOffset = -1;
		}

		protected Reader read() throws IOException {
			return new InputStreamReader(textBuffer.getSupplier().getInput(), charset);
		}

		protected void dispose() throws IOException {
			textBuffer.reset();
		}

		public void read(Reader content, int contentLength) throws IOException {
			reader = null;
			try {
				reader = xmlInputFactory.createXMLStreamReader(content);
				int xmlEvents = 0;
				Map<QName, String> attributes = null;

				while (reader.hasNext()) {
					if (xmlEvents++ % xmlEventBatchSize == 0) {
						newXMLEventBatch();
					}

					switch (reader.next()) {
					case XMLStreamConstants.START_ELEMENT:
						writeText();
						nextSibling();

						attributes = Maps.newHashMap();
						final int attributeCount = reader.getAttributeCount();
						for (int ac = 0; ac < attributeCount; ac++) {
							final javax.xml.namespace.QName attrQName = reader.getAttributeName(ac);
							if (XMLNS_ATTRIBUTE_NS_URI.equals(attrQName.getNamespaceURI())) {
								continue;
							}
							attributes.put(new QNameImpl(attrQName), reader.getAttributeValue(ac));
						}

						startAnnotation(new QNameImpl(reader.getName()), attributes);
						break;
					case XMLStreamConstants.END_ELEMENT:
						writeText();
						endAnnotation();
						break;
					case XMLStreamConstants.COMMENT:
						writeText();
						nextSibling();

						attributes = Maps.newHashMap();
						attributes.put(QNameImpl.COMMENT_TEXT_QNAME, reader.getText());
						startAnnotation(QNameImpl.COMMENT_QNAME, attributes);
						endAnnotation();
						break;
					case XMLStreamConstants.PROCESSING_INSTRUCTION:
						writeText();
						nextSibling();

						attributes = Maps.newHashMap();
						attributes.put(QNameImpl.PI_TARGET_QNAME, reader.getPITarget());
						final String data = reader.getPIData();
						if (data != null) {
							attributes.put(QNameImpl.PI_DATA_QNAME, data);
						}

						startAnnotation(QNameImpl.PI_QNAME, attributes);
						endAnnotation();
						break;
					case XMLStreamConstants.CHARACTERS:
					case XMLStreamConstants.ENTITY_REFERENCE:
					case XMLStreamConstants.CDATA:
						text();
						break;
					case XMLStreamConstants.END_DOCUMENT:
						end();
						break;
					}
				}

				Reader textContentReader = null;
				try {
					textContentReader = read();
					final int textContentLength = contentLength(textContentReader);
					Closeables.close(textContentReader, false);

					textContentReader = read();
					updateText(target, textContentReader, textContentLength);
				} finally {
					Closeables.closeQuietly(textContentReader);
				}
			} catch (XMLStreamException e) {
				throw new RuntimeException(e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (XMLStreamException e) {
					}
				}
			}
		}
	}
}
