package net.srikanths.clipboardpingpong;

public class SocketAddress {
  private String host;
  private int port;

  public SocketAddress(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }
}
