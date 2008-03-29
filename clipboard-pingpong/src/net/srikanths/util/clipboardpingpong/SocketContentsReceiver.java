package net.srikanths.util.clipboardpingpong;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SocketContentsReceiver implements ContentsReceiver, Runnable {
  private ClipboardHelper clipboardHelper;
  private ServerSocket serverSocket;

  public SocketContentsReceiver(int port, ClipboardHelper helper)
      throws IOException {
    setClipboardHelper(helper);
    serverSocket = new ServerSocket(port);
    serverSocket.setSoTimeout(10 * 1000);
  }

  public void changeSystemClipboardContents(String contents) {
    clipboardHelper.setContents(contents);
  }

  public void setClipboardHelper(ClipboardHelper helper) {
    this.clipboardHelper = helper;
  }

  public void run() {
    while (true) {
      try {
        Socket client = serverSocket.accept();
        DataInputStream inputStream
            = new DataInputStream(client.getInputStream());
        int contentsSize = inputStream.readInt();
        byte[] contents = new byte[contentsSize];
        inputStream.readFully(contents);
        String newContents = new String(contents);
        System.out.println("Received new contents, changing clipboard to: "
            + newContents);
        changeSystemClipboardContents(newContents);
      } catch (SocketTimeoutException e) {
        // Socket timeout, continue for now.
      } catch (IOException e) {
        // TODO Do something.
        e.printStackTrace();
      }
    }
  }
}
