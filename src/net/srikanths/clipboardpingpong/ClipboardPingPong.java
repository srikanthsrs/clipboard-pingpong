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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author Srikanth S
 */
public class ClipboardPingPong {
  public ClipboardPingPong(String[] args) {
    BuildProperties buildProperties;
    try {
      buildProperties = new BuildProperties();
    } catch (IOException e) {
      System.out.println(e.getMessage());
      return;
    }

    PingPongOptParser optParser;
    try {
      optParser = new PingPongOptParser(args, buildProperties);
    } catch (OptParseException e) {
      System.out.println(e.getMessage() + C.NL);
      System.out.println("Please make sure you have configured the server.");
      return;
    }

    if (optParser.isVersion()) {
      System.out.println(optParser.getVersion());
      return;
    }

    if (optParser.isShutdown()) {
      ShutdownHelper shutdownHelper = new ShutdownHelper();
      shutdownHelper.shutdown();
      return;
    }

    PingPongConfiguration configuration = new PingPongConfiguration();
    try {
      configuration.loadProperties();
    } catch (InvalidPingPongConfigurationException e) {
      // TODO(srikanths): Should open the configuration utility in future.
      System.out.println(e.getMessage());
      return;
    }

    if (optParser.isStartServer()) {
      startServer(configuration);
      return;
    }

    assert false : "Should never reach here.";
    System.out.println(optParser.getUsage());
  }

  private void startServer(PingPongConfiguration configuration) {
    ShutdownInitiator shutdownInitiator;
    try {
      shutdownInitiator = new ShutdownInitiator();
    } catch (IOException e) {
      System.out.println(e.getMessage());
      return;
    }

    Thread shutdownThread = new Thread(shutdownInitiator);
    shutdownThread.setName("ShutdownInitiator");
    shutdownThread.start();

    ClipboardHelper clipboardHelper = new DefaultClipboardHelper();
    ContentsDistributor contentsDistributor = new SocketContentsDistributor(
        configuration.getReceiverAddresses());
    clipboardHelper.addContentsListener(contentsDistributor);
    ContentsReceiver contentsReceiver;
    try {
      contentsReceiver = new SocketContentsReceiver(
          configuration.getServerPort(), clipboardHelper);
    } catch (IOException e) {
      System.out.println(e.getMessage());
      return;
    }

    shutdownInitiator.addShutdownListener(contentsReceiver);
    Thread contentReceiverThread = new Thread(contentsReceiver);
    contentReceiverThread.setName("ContentsReceiver");
    contentReceiverThread.start();

    System.out.println("Server started.");
  }

  private static class PingPongOptParser {
    private static final int TOTAL_OPTION_NAME_DESCRIPTION_LENGTH = 24;
    private BuildProperties buildProperties;

    private boolean version;
    private boolean startServer;
    private boolean shutdown;

    //
    // Command line options supported.
    //
    private static final String VERSION_OPT_LONG = "--version";
    private static final String VERSION_OPT_SHORT = "-version";

    private static final String SHUTDOWN_OPT_LONG = "--shutdown";
    private static final String SHUTDOWN_OPT_SHORT = "-x";

    private String[] validOptions = {
        VERSION_OPT_LONG, VERSION_OPT_SHORT,
        SHUTDOWN_OPT_LONG, SHUTDOWN_OPT_SHORT
    };

    public PingPongOptParser(String[] args, BuildProperties buildProperties)
        throws OptParseException {
      this.buildProperties = buildProperties;

      List options = new Vector();

      if (args != null && args.length != 0) {
        for (int i = 0; i < args.length; i++) {
          options.add(args[i]);
        }
      }

      if (options.contains(VERSION_OPT_LONG)
          || options.contains(VERSION_OPT_SHORT)) {
        version = true;
      }

      if (options.contains(SHUTDOWN_OPT_LONG)
          || options.contains(SHUTDOWN_OPT_SHORT)) {
        shutdown = true;
      }

      List validOptionsAsList = Arrays.asList(validOptions);
      options.removeAll(validOptionsAsList);
      if (options.size() > 0) {
        String error = "Invalid option(s):" + C.NL
            + C.TAB + options + C.NL + C.NL
            + getUsage();
        throw new OptParseException(error);
      }

      if (!(shutdown || version)) {
        startServer = true;
      }
    }

    public boolean isVersion() {
      return version;
    }

    public boolean isStartServer() {
      return startServer;
    }

    public boolean isShutdown() {
      return shutdown;
    }

    public String getUsage() {
      StringBuffer usage = new StringBuffer();
      usage.append("Usage: java -jar ")
          .append(buildProperties.get(BuildProperties.PROJECT_JAR_NAME))
          .append(" [").append(VERSION_OPT_LONG).append(" | ")
          .append(SHUTDOWN_OPT_LONG).append("]").append(C.NL).append(C.NL)

          .append("*** NO OPTIONS == START SERVER ***").append(C.NL)
          .append(C.TAB).append(C.TAB)
          .append("To start the server, please don't supply any options.")
          .append(C.NL).append(C.NL)
          .append("where options include:").append(C.NL)
          .append(C.TAB)
          .append(pad(VERSION_OPT_LONG + " | " + VERSION_OPT_SHORT))
          .append(" --> To print the version of the software.").append(C.NL)

          .append(C.TAB)
          .append(pad(SHUTDOWN_OPT_LONG + " | " + SHUTDOWN_OPT_SHORT))
          .append(" --> To shutdown the server if running.").append(C.NL);
      return usage.toString();
    }

    public String getVersion() {
      StringBuffer version = new StringBuffer();
      version.append(buildProperties.get(BuildProperties.PROJECT_NAME))
          .append(" version \"")
          .append(buildProperties.get(BuildProperties.PROJECT_VERSION))
          .append("\"")
          .append(C.NL)
          .append("Project Home: ")
          .append(buildProperties.get(BuildProperties.PROJECT_URL))
          .append(C.NL).append(C.NL)
          .append(getUsage());
      return version.toString();
    }

    private String pad(String string) {
      if (string.length() >= TOTAL_OPTION_NAME_DESCRIPTION_LENGTH) {
        return string;
      }

      int padding = TOTAL_OPTION_NAME_DESCRIPTION_LENGTH - string.length();
      StringBuffer totalPadding = new StringBuffer();
      for (int i = 0; i < padding; i++) {
        totalPadding.append(" ");
      }

      return string + totalPadding.toString();
    }
  }

  private static class OptParseException extends Exception {
    private static final long serialVersionUID = 1L;

    public OptParseException(String message) {
      super(message);
    }

    public OptParseException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static void main(String[] args) {
    new ClipboardPingPong(args);
  }
}
