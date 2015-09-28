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

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author Srikanth S
 */
public class ShutdownInitiator implements Runnable {
  public static final String SHUTDOWN_FILE_DELIMITER = "=";
  public static final String SHUTDOWN_COMMAND_PREFIX = "shutdown=";
  public static final String SHUTDOWN_ID_FILE_PATH
      = C.APP_DATA_DIR + C.FS +".shutdown";

  private List shutdownListeners;
  private boolean shutdownInitiated;
  private ServerSocket shutdownServerSocket;
  private File shutdownIdFile;
  private String shutdownId;
  private boolean shutdown;

  public ShutdownInitiator() throws IOException {
    shutdownListeners = new Vector();
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        System.out.println("Shutting down the server...");
        ShutdownInitiator.this.startShutdown();
      }
    });

    try {
      shutdownServerSocket = new ServerSocket();
      shutdownServerSocket.bind(null);
      shutdownServerSocket.setSoTimeout(C.SO_TIMEOUT);
    } catch (IOException e) {
      String error = "Could not open port for server shutdown socket."
          + C.NL + e.getMessage();
      IOException modifiedException = new IOException(error);
      modifiedException.setStackTrace(e.getStackTrace());
    }

    shutdownIdFile = new File(SHUTDOWN_ID_FILE_PATH);
    if (shutdownIdFile.exists()) {
      if (!shutdownIdFile.isFile()) {
        String error = "The path to the shutdown ID file: '"
          + Util.getFullName(shutdownIdFile) + "' is not a regular file."
          + C.NL + "Please remove this manually.";
        throw new IOException(error);
      }

      shutdownIdFile.delete();
    }

    try {
      createShutdownIdFile();
    } catch (IOException e) {
      String error = "Shutdown ID file cannot be created." + C.NL
          + e.getMessage();
      IOException modifiedException = new IOException(error);
      modifiedException.setStackTrace(e.getStackTrace());
    }
  }

  public void addShutdownListener(ShutdownListener listener) {
    shutdownListeners.add(listener);
  }

  public void removeShutdownListener(ShutdownListener listener) {
    shutdownListeners.remove(listener);
  }

  public void run() {
    while (!shutdown) {
      try {
        Socket client = shutdownServerSocket.accept();
        DataInputStream inputStream
            = new DataInputStream(client.getInputStream());
        int length = inputStream.readInt();
        byte[] data = new byte[length];
        inputStream.readFully(data);
        DataOutputStream outputStream
            = new DataOutputStream(client.getOutputStream());
        String request = new String(data);
        if (!request.startsWith(SHUTDOWN_COMMAND_PREFIX)) {
          String response = "Invalid Request.";
          outputStream.writeInt(response.length());
          outputStream.write(response.getBytes());
          outputStream.close();
          return;
        }

        String shutdownIDSent =
            request.replaceFirst(SHUTDOWN_COMMAND_PREFIX, "");
        if (!shutdownIDSent.equals(shutdownId)) {
          String response = "Invalid shutdown ID.";
          outputStream.writeInt(response.length());
          outputStream.write(response.getBytes());
          outputStream.close();
          return;
        }

        shutdown = true;

        startShutdown();
        String response = "Shutdown successful.";
        outputStream.writeInt(response.length());
        outputStream.write(response.getBytes());
        outputStream.close();

        Util.closeServerSocketSilently(shutdownServerSocket);
      } catch (SocketTimeoutException e) {
        // We will just continue.
      } catch (IOException e) {
        System.out.println("IOException: " + e.getMessage());
      }
    }
  }

  public void startShutdown() {
    // If shutdown has already been initiated, just exit.
    synchronized (this) {
      if (shutdownInitiated) {
        return;
      }

      shutdownInitiated = true;
    }

    Object[] listeners = new Object[shutdownListeners.size()];
    synchronized (shutdownListeners) {
      listeners = shutdownListeners.toArray();
    }

    for (int i = 0; i < listeners.length; i++) {
      ShutdownListener shutdownListener = (ShutdownListener) listeners[i];
      shutdownListener.shutdown();
    }
  }

  private void createShutdownIdFile() throws IOException {
    int shutdownPort = shutdownServerSocket.getLocalPort();
    shutdownIdFile.createNewFile();
    shutdownIdFile.deleteOnExit();
    shutdownId = getUniqueShutdownId();
    BufferedOutputStream outputStream
        = new BufferedOutputStream(new FileOutputStream(shutdownIdFile));
    String fileContents = Integer.toString(shutdownPort)
        + SHUTDOWN_FILE_DELIMITER + shutdownId;
    outputStream.write(fileContents.getBytes());
    outputStream.close();
    shutdownIdFile.setReadOnly();
  }

  /**
   * <p>
   * Returns a Unqiue ID that should be sent when requesting for shutdown.
   *
   * <p>
   * The Unique ID is:
   * <code>String(current_time_stamp) + String(a_random_1_digit_number)</code>.
   * This is no where closer to GUID, but GUID will be an overkill for this
   * simple application.
   *
   * @return a Unqiue ID that should be sent when requesting for shutdown.
   */
  private String getUniqueShutdownId() {
    long currentTime = System.currentTimeMillis();
    int random = (int) (Math.random() * 10);
    return "" + currentTime + random;
  }
}
