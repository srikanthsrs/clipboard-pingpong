package net.srikanths.clipboardpingpong;

public interface ContentsReceiver extends Runnable {
  public void setClipboardHelper(ClipboardHelper helper);

  public void changeSystemClipboardContents(String contents);
}
