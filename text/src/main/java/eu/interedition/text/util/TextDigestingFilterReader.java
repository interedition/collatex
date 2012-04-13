package eu.interedition.text.util;

import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import eu.interedition.text.Text;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public class TextDigestingFilterReader extends FilterReader {
  public static final byte[] NULL_DIGEST = digest("");

  public MessageDigest digest;
  public byte[] result;
  public CharsetEncoder encoder;

  public TextDigestingFilterReader(Reader in) {
    super(in);
    try {
      this.digest = MessageDigest.getInstance("SHA-512");
      this.encoder = Text.CHARSET.newEncoder();
    } catch (NoSuchAlgorithmException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public int read() throws IOException {
    final int read = super.read();
    if (read >= 0) {
      digest.update(encoder.encode(CharBuffer.wrap(new char[]{(char) read})));
    }
    return read;
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    final int read = super.read(cbuf, off, len);
    if (read >= 0) {
      digest.update(encoder.encode(CharBuffer.wrap(cbuf, off, len)));
    }
    return read;
  }

  @Override
  public void reset() throws IOException {
    digest.reset();
    result = null;
    super.reset();
  }

  public byte[] digest() {
    if (result == null) {
      result = digest.digest();
      digest.reset();
    }
    return result;
  }

  public static byte[] digest(String str) {
    final TextDigestingFilterReader reader = new TextDigestingFilterReader(new StringReader(str));
    try {
      while (reader.read() >= 0) {
      }
      return reader.digest();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    } finally {
      Closeables.closeQuietly(reader);
    }
  }
}
