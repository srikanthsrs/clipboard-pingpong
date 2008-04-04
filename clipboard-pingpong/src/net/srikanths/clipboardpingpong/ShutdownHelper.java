package net.srikanths.clipboardpingpong;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ShutdownHelper {
  public ShutdownHelper() {}

  public void shutdown() {
    File shutdownIdFile = new File(ShutdownInitiator.SHUTDOWN_ID_FILE_PATH);
    if (!shutdownIdFile.isFile()) {
      String message = "The shutdown ID file '"
          + Util.getFullName(shutdownIdFile) + "' does not exist." + C.NL
          + "Please do a hard shutdown directly if the server is running.";
      System.out.println(message);
      return;
    }

    String fileContents;
    try {
      BufferedInputStream shutdownIdFileInputStream = new BufferedInputStream(
          new FileInputStream(shutdownIdFile));
      StringBuffer shutdownIdBuffer = new StringBuffer();
      while (true) {
        byte[] buffer = new byte[1024];
        int bytesRead = shutdownIdFileInputStream.read(buffer);
        if (bytesRead == -1) {
          break;
        }

        shutdownIdBuffer.append(new String(buffer, 0, bytesRead));
      }
      shutdownIdFileInputStream.close();
      fileContents = shutdownIdBuffer.toString();
    } catch (IOException e) {
      String message = "Error while reading the shutdown ID file '"
          + Util.getFullName(shutdownIdFile) + "'." + C.NL + e.getMessage()
          + C.NL
          + "Please do a hard shutdown directly if the server is running.";
      System.out.println(message);
      return;
    }

    String invalidFileMessage = "The shutdown ID file '"
        + Util.getFullName(shutdownIdFile) + "' is invalid." + C.NL
        + "Please do a hard shutdown directly if the server is running.";
    if (fileContents.length() == 0) {
      System.out.println(invalidFileMessage);
      return;
    }

    int indexOfDelimiter =
        fileContents.indexOf(ShutdownInitiator.SHUTDOWN_FILE_DELIMITER);
    if (indexOfDelimiter == -1) {
      System.out.println(invalidFileMessage);
      return;
    }

    String portAsString = fileContents.substring(0, indexOfDelimiter);
    int port;
    try {
      port = Integer.parseInt(portAsString);
    } catch (NumberFormatException e) {
      System.out.println(invalidFileMessage);
      return;
    }

    String shutdownId = fileContents.substring(indexOfDelimiter + 1);
    String response;
    try {
      Socket shutdownSocket
          = new Socket(InetAddress.getLocalHost().getHostName(), port);
      String shutdownCommand
          = ShutdownInitiator.SHUTDOWN_COMMAND_PREFIX + shutdownId;
      DataOutputStream shutdownSocketOutputStream
          = new DataOutputStream(shutdownSocket.getOutputStream());
      shutdownSocketOutputStream.writeInt(shutdownCommand.length());
      shutdownSocketOutputStream.write(shutdownCommand.getBytes());
      shutdownSocketOutputStream.flush();

      DataInputStream shutdownSocketInputStream
          = new DataInputStream(shutdownSocket.getInputStream());
      int responseLength = shutdownSocketInputStream.readInt();
      byte[] responseData = new byte[responseLength];
      shutdownSocketInputStream.readFully(responseData);
      response = new String(responseData);
      shutdownSocket.close();
    } catch (IOException e) {
      String error = "The server may be already down." + C.NL + C.NL
          + "Internal message: " + e.getMessage();
      System.out.println(error);
      return;
    }

    System.out.println("Message from server: " + response);
  }
}
