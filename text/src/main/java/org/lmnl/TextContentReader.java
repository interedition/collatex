package org.lmnl;

import java.io.IOException;
import java.io.Reader;

public interface TextContentReader {

	void read(Reader content, int contentLength) throws IOException;
}
