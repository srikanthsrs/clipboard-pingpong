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
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Srikanth S
 */
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
