package net.srikanths.clipboardpingpong;

import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.UnsupportedFlavorException;

public interface ClipboardHelper extends ClipboardOwner {
  public void setContents(String contents);

  public String getContents() throws UnsupportedFlavorException;

  public void addContentsListener(ContentsListener listener);

  public void removeContentsListener(ContentsListener listener);
}
