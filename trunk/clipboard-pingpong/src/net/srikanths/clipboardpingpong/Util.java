package net.srikanths.clipboardpingpong;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

public class Util {
  private Util() {
    // Private constructor to avoid instantiation.
  }

  public static String getFullName(File file) {
    if (file == null) {
      return null;
    }

    try {
      return file.getCanonicalPath();
    } catch (IOException e) {
      return file.getAbsolutePath();
    }
  }

  public static boolean isValidPort(int port) {
    return (port > 1024) && (port <= 65535);
  }

  public static void sleepSilently(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {}
  }

  public static void closeServerSocketSilently(ServerSocket serverSocket) {
    try {
      serverSocket.close();
    } catch (IOException e) {}
  }
}
