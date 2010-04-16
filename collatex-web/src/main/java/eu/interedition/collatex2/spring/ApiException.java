package eu.interedition.collatex2.spring;

public class ApiException extends Exception {

  public ApiException() {
    super();
  }

  public ApiException(String message, Throwable cause) {
    super(message, cause);
  }

  public ApiException(String message) {
    super(message);
  }

  public ApiException(Throwable cause) {
    super(cause);
  }
}
