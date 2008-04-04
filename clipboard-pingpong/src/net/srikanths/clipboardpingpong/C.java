package net.srikanths.clipboardpingpong;

import java.io.File;

/**
 * <p>
 * A class that contains the constants that have application-wide usage.
 *
 * @author sri1025 (Srikanth S)
 */
public class C {
  //
  // Widely used constants.
  //
  /** Platform's new line character. */
  public static final String NL = System.getProperty("line.separator", "\n");
  /** Platform's file separator character. */
  public static final String FS = File.separator;

  //
  // Application related constants.
  //
  /** Default application directory */
  public static final String APP_DATA_DIR = "." + FS + "app-data";
  /** Default Socket timeout. */
  public static final int SO_TIMEOUT = 3 * 1000;
  /** Standard TAB (2 spaces) used in the application. */
  public static final String TAB = "  ";

  private C() {
    // Private constructor to avoid instantiation.
  }
}
