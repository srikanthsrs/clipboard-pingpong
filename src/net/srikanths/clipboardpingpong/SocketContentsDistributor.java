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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author Srikanth S
 */
public class SocketContentsDistributor implements ContentsDistributor {
  private SocketAddress[] addresses;

  public SocketContentsDistributor(SocketAddress[] addresses) {
    if (null == addresses) {
      throw new NullPointerException("Addresses can't be null.");
    }
    this.addresses = addresses;
  }

  public void distribute(String contents) {
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
        // TODO(srikanths): We need to do more here than just spit out the
        // error message.
        String message = "Could not send new contents to: " + socketAddress;
        System.out.println(message);
      }
    }
  }

  public void newContentsReceived(String contents) {
    distribute(contents);
  }
}
