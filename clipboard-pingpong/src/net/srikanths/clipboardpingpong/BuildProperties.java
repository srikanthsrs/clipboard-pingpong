package net.srikanths.clipboardpingpong;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BuildProperties {
  private Properties properties;
  private String BUILD_PROPERTIES_FILE_RESOURCE_PATH = "/build.properties";

  // Known property keys.
  public static final String PROJECT_NAME = "project.name";
  public static final String PROJECT_VERSION = "project.version";
  public static final String PROJECT_URL = "project.url";
  public static final String PROJECT_JAR_NAME = "project.jarname";
  public static final String PROJECT_BUILT_BY = "project.built-by";

  public BuildProperties() throws IOException {
    properties = new Properties();
    InputStream buildPropertiesInputStream
        = getClass().getResourceAsStream(BUILD_PROPERTIES_FILE_RESOURCE_PATH);
    try {
      properties.load(buildPropertiesInputStream);
    } catch (IOException e) {
      String error = "No " + BUILD_PROPERTIES_FILE_RESOURCE_PATH + " file "
          + "found in resource." + C.NL + "Installation is corrupted.";
      IOException modified = new IOException(error);
      modified.setStackTrace(e.getStackTrace());
      throw modified;
    }
  }

  public String get(String key) {
    if (key == null) {
      return "";
    }

    return properties.getProperty(key, "");
  }
}
