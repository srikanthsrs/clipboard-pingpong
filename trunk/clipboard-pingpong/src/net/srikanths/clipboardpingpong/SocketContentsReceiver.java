/*
 * Copyright (C) 2007 Free Software Foundation, Inc.
 *
 * Licensed under the GNU General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/gpl.html
 *
 * Software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.srikanths.clipboardpingpong;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 *
 * @author Srikanth S
 */
public class SocketContentsReceiver implements ContentsReceiver, Runnable {
  private ClipboardHelper clipboardHelper;
  private ServerSocket serverSocket;
  private boolean shutdown;

  public SocketContentsReceiver(int port, ClipboardHelper helper)
      throws IOException {
    setClipboardHelper(helper);

    try {
      serverSocket = new ServerSocket(port);
      serverSocket.setSoTimeout(C.SO_TIMEOUT);
    } catch (IOException e) {
      String error = "Cannot open Server at port: '" + port + "'"
          + C.NL + e.getMessage();
      IOException modifiedException = new IOException(error);
      modifiedException.setStackTrace(e.getStackTrace());
      throw modifiedException;
    }
  }

  public void fireSystemClipboardChange(String contents) {
    clipboardHelper.setContents(contents);
  }

  public void setClipboardHelper(ClipboardHelper helper) {
    this.clipboardHelper = helper;
  }

  public void run() {
    while (true) {
      synchronized (this) {
        if (shutdown) {
          System.out.println("Closing SocketContentsReceiver thread...");
          return;
        }
      }

      try {
        Socket client = serverSocket.accept();
        DataInputStream inputStream
            = new DataInputStream(client.getInputStream());
        int contentsSize = inputStream.readInt();
        byte[] contents = new byte[contentsSize];
        inputStream.readFully(contents);
        String newContents = new String(contents);
        fireSystemClipboardChange(newContents);
      } catch (SocketTimeoutException e) {
        // Socket timeout, continue for now.
      } catch (IOException e) {
        System.out.println("IOException: " + e.getMessage());
      }
    }
  }

  public synchronized void shutdown() {
    shutdown = true;
    try {
      serverSocket.close();
    } catch (IOException e) {
      System.out.println(
          "Error while closing the contents receiver server socket.;");
    }
  }
}
