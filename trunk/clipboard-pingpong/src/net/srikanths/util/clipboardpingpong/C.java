package net.srikanths.util.clipboardpingpong;

import java.io.File;

public class C {
  // Widely used constants.
  public static final String NL = System.getProperty("line.separator", "\n");
  public static final String FS = File.separator;

  // Application related constants.
  public static final String APP_DATA_DIR = "." + FS + "app-data";

  private C() {
    // Private constructor to avoid instantiation.
  }
}
