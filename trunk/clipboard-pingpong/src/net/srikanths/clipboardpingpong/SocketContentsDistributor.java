package net.srikanths.clipboardpingpong;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketContentsDistributor implements ContentsDistributor {
  private SocketAddress[] addresses;

  public SocketContentsDistributor(SocketAddress[] addresses) {
    if (null == addresses) {
      throw new NullPointerException("Addresses can't be null.");
    }
    this.addresses = addresses;
  }

  public void distribute(String contents) {
    System.out.println("Distributing new clipboard contents to listeners: "
        + contents);
    for (int i = 0; i < addresses.length; i++) {
      SocketAddress socketAddress = addresses[i];
      try {
        Socket socket = new Socket(socketAddress.getHost(),
            socketAddress.getPort());
        DataOutputStream outputStream =
            new DataOutputStream(socket.getOutputStream());
        outputStream.writeInt(contents.length());
        outputStream.write(contents.getBytes());
        outputStream.flush();
      } catch (IOException e) {
        // TODO(srikanths): Handle this.
        e.printStackTrace();
      }
    }
  }

  public void newContentsReceived(String contents) {
    distribute(contents);
  }
}
