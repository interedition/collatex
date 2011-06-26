package org.lmnl;

import java.io.IOException;
import java.io.Reader;
import java.util.SortedMap;
import java.util.SortedSet;

public interface TextRepository {

	int length(Text text) throws IOException;

	void read(Text text, TextContentReader reader) throws IOException;

	String read(Text text, Range range) throws IOException;
	
	SortedMap<Range, String> bulkRead(Text text, SortedSet<Range> ranges) throws IOException;

	void write(Text text, Reader contents, int contentLength) throws IOException;
}
