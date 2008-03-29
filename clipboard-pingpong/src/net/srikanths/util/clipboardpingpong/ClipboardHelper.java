package net.srikanths.util.clipboardpingpong;

import java.awt.datatransfer.ClipboardOwner;

public interface ClipboardHelper extends ClipboardOwner {
  public void setContents(String contents);

  public String getContents();

  public void addContentsListener(ContentsListener listener);

  public void removeContentsListener(ContentsListener listener);
}
