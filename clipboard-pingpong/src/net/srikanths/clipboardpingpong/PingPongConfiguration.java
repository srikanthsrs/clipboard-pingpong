package net.srikanths.clipboardpingpong;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class PingPongConfiguration {
  public static final int DEFAULT_SERVER_PORT = 55555;
  public static final String CONFIGURATION_FILE_NAME = "pingpong.conf";

  private int serverPort;
  private List receiverAddresses;

  public PingPongConfiguration() {
    receiverAddresses = new Vector();
  }

  public int getServerPort() {
    return serverPort;
  }

  public SocketAddress[] getReceiverAddresses() {
    SocketAddress[] socketAddresses;
    synchronized (receiverAddresses) {
      socketAddresses = new SocketAddress[receiverAddresses.size()];
      socketAddresses = (SocketAddress[]) receiverAddresses.toArray(
          socketAddresses);
    }

    return socketAddresses;
  }

  public void loadProperties()
      throws InvalidPingPongConfigurationException {
    clear();

    // Be as strict as you can.
    File configurationFile
        = new File(C.APP_DATA_DIR, CONFIGURATION_FILE_NAME);
    if (!configurationFile.exists()) {
      String message = "The configuration file '"
          + Util.getFullName(configurationFile) + "' is missing.";
      throw new InvalidPingPongConfigurationException(message);
    }

    if (!configurationFile.isFile()) {
      String message = "The path '" + Util.getFullName(configurationFile)
          + "' is not a file. " + C.NL
          + "The application expects a configuration file here.";
      throw new InvalidPingPongConfigurationException(message);
    }

    try {
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(new FileInputStream(configurationFile)));
      String line = null;
      ConfigurationAttributes expected = ConfigurationAttributes.SERVER_SECTION;

      for (int lineNumber = 1;; lineNumber++) {
        line = reader.readLine();
        if (line == null) {
          break;
        }

        line = line.trim();
        if (line.length() == 0) {
          continue;
        }

        if (expected == ConfigurationAttributes.SERVER_SECTION) {
          if (!ConfigurationAttributes.isServerSection(line)) {
            String message = "Invalid configuration, expected server section."
                + C.NL + "Error at line number: " + lineNumber
                + " in " + Util.getFullName(configurationFile);
            throw new InvalidPingPongConfigurationException(message);
          }

          expected = ConfigurationAttributes.SERVER_PORT_ENTRY;
        } else if (expected == ConfigurationAttributes.SERVER_PORT_ENTRY) {
          if (!ConfigurationAttributes.isServerPortEntry(line)) {
            String message = "Invalid configuration, expected server port "
                + "entry." + C.NL + "Error at line number: " + lineNumber
                + " in " + Util.getFullName(configurationFile);
            throw new InvalidPingPongConfigurationException(message);
          }

          String portAsString = line.replaceFirst(
              ConfigurationAttributes.SERVER_PORT_ENTRY_PREFIX , "");
          String invalidPortMessage = "Invalid configuration file. "
              + "Server port must be a number between 1025 and 65535."
              + C.NL + "Error at line number: " + lineNumber
              + " in " + Util.getFullName(configurationFile);
          int port;
          try {
            port = Integer.parseInt(portAsString);
          } catch (NumberFormatException e) {
            throw new InvalidPingPongConfigurationException(invalidPortMessage);
          }

          if (!Util.isValidPort(port)) {
            throw new InvalidPingPongConfigurationException(invalidPortMessage);
          }

          serverPort = port;
          expected = ConfigurationAttributes.RECEIVERS_SECTION;
        } else if (expected == ConfigurationAttributes.RECEIVERS_SECTION) {
          if (!ConfigurationAttributes.isReceiversSection(line)) {
            String message = "Invalid configuration, expected receivers "
                + "section." + C.NL + "Error at line number: " + lineNumber
                + " in " + Util.getFullName(configurationFile);
            throw new InvalidPingPongConfigurationException(message);
          }

          expected = ConfigurationAttributes.RECEIVER_ENTRY;
        } else if (expected == ConfigurationAttributes.RECEIVER_ENTRY) {
          if (!ConfigurationAttributes.isReceiverEntry(line)) {
            String message = "Invalid configuration, expected receiver's entry."
                + C.NL + "Error at line number: " + lineNumber
                + " in " + Util.getFullName(configurationFile);
            throw new InvalidPingPongConfigurationException(message);
          }

          int indexOfReceiverEntrySeparator
              = line.indexOf(ConfigurationAttributes.ENTRY_SEPARATOR);
          String host = line.substring(0, indexOfReceiverEntrySeparator);
          String portAsString
              = line.substring(indexOfReceiverEntrySeparator + 1);
          String invalidPortMessage = "Invalid configuration file. "
              + "Receiver's port must be a number between 1025 and 65535."
              + C.NL + "Error at line number: " + lineNumber
              + " in " + Util.getFullName(configurationFile);
          int port;
          try {
            port = Integer.parseInt(portAsString);
          } catch (NumberFormatException e) {
            throw new InvalidPingPongConfigurationException(invalidPortMessage);
          }

          if (!Util.isValidPort(port)) {
            throw new InvalidPingPongConfigurationException(invalidPortMessage);
          }

          SocketAddress receiverAddress = new SocketAddress(host, port);
          synchronized (receiverAddress) {
            receiverAddresses.add(receiverAddress);
          }
        }
      }

      // Now, our expected must be ConfigurationAttributes.RECEIVER_ENTRY
      if (expected != ConfigurationAttributes.RECEIVER_ENTRY) {
        String message = "Invalid configuration file: "
            + Util.getFullName(configurationFile);;
        throw new InvalidPingPongConfigurationException(message);
      }
    } catch (IOException e) {
      String message = "Error while reading the configuraion file '"
        + Util.getFullName(configurationFile) + "'.";
      throw new InvalidPingPongConfigurationException(message, e);
    }
  }

  public void storeProperties()
      throws InvalidPingPongConfigurationException {
    // TODO(srikanths): To be completed.
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(ConfigurationAttributes.SERVER_SECTION).append(C.NL)
        .append(ConfigurationAttributes.SERVER_PORT_ENTRY_PREFIX)
        .append(serverPort).append(C.NL)
        .append(C.NL)
        .append(ConfigurationAttributes.RECEIVERS_SECTION).append(C.NL);
    synchronized (receiverAddresses) {
      Iterator iterator = receiverAddresses.iterator();
      while (iterator.hasNext()) {
        SocketAddress socketAddress = (SocketAddress) iterator.next();
        buffer.append(socketAddress.getHost())
            .append(ConfigurationAttributes.ENTRY_SEPARATOR)
            .append(socketAddress.getPort()).append(C.NL);
      }
    }
    buffer.append(C.NL);
    return buffer.toString();
  }

  private void clear() {
    synchronized (receiverAddresses) {
      serverPort = 0;
      receiverAddresses.clear();
    }
  }

  private static class ConfigurationAttributes {
    // Attribute constants.
    public static final ConfigurationAttributes SERVER_SECTION
        = new ConfigurationAttributes("[server]");
    public static final ConfigurationAttributes SERVER_PORT_ENTRY
        = new ConfigurationAttributes("SERVER_PORT_ENTRY");
    public static final ConfigurationAttributes RECEIVERS_SECTION
        = new ConfigurationAttributes("[receivers]");
    public static final ConfigurationAttributes RECEIVER_ENTRY
        = new ConfigurationAttributes("RECEIVER_ENTRY");

    // Other constants.
    public static final String SERVER_PORT_ENTRY_PREFIX = "port=";
    public static final String ENTRY_SEPARATOR = "=";
    private static final char ENTRY_SEPARATOR_AS_CHAR
        = ENTRY_SEPARATOR.charAt(0);

    private String attributeName;

    private ConfigurationAttributes(String attributeName) {
      // This class is used as an enum.
      this.attributeName = attributeName;
    }

    public String getAttributeName() {
      return attributeName;
    }

    public int hashCode() {
      return attributeName.hashCode();
    }

    public String toString() {
      return attributeName;
    }

    public static boolean isServerSection(String entry) {
      return entry.equals(SERVER_SECTION.toString());
    }

    public static boolean isServerPortEntry(String entry) {
      return entry.startsWith(SERVER_PORT_ENTRY_PREFIX);
    }

    public static boolean isReceiversSection(String entry) {
      return entry.equals(RECEIVERS_SECTION.toString());
    }

    public static boolean isReceiverEntry(String entry) {
      return ((entry.indexOf(ENTRY_SEPARATOR) != -1)
          && (entry.charAt(0) != ENTRY_SEPARATOR_AS_CHAR)
          && (entry.charAt(entry.length() - 1) != ENTRY_SEPARATOR_AS_CHAR));
    }
  }
}
