package net.srikanths.util.clipboardpingpong;

public class InvalidPingPongConfigurationException extends Exception {
  private static final long serialVersionUID = 1L;

  public InvalidPingPongConfigurationException(String message) {
    super(message);
  }

  public InvalidPingPongConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}
