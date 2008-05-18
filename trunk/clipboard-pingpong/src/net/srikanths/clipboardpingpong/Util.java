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

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

/**
 *
 * @author Srikanth S
 */
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
